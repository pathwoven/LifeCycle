package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.constant.MqConstants;
import com.cc.constant.RedisConstants;
import com.cc.dto.SeckillOrderMessage;
import com.cc.entity.VoucherOrder;
import com.cc.mapper.VoucherOrderMapper;
import com.cc.service.ISeckillVoucherService;
import com.cc.service.IVoucherOrderService;
import com.cc.utils.RedisIDWorker;
import com.google.common.collect.Lists;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.beancontext.BeanContext;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisIDWorker redisIDWorker;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private VoucherOrderMapper voucherOrderMapper;
    @Autowired
    private ApplicationContext applicationContext;
    private static DefaultRedisScript<Long> SECKILL_SCRIPT;
    {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setResultType(Long.class);
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
    }
    private static DefaultRedisScript<Long> SECKILL_CANCEL_SCRIPT;
    {
        SECKILL_CANCEL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_CANCEL_SCRIPT.setResultType(Long.class);
        SECKILL_CANCEL_SCRIPT.setLocation(new ClassPathResource("seckillCancel.lua"));
    }
    @Override
    public Long seckillVoucher(Long userId, Long voucherId) {
        String stockKey = RedisConstants.SECKILL_STOCK_KEY+voucherId;
        // 判断是否在抢购期
        LocalDateTime begin = (LocalDateTime) stringRedisTemplate.opsForHash().get(stockKey, "begin");
        LocalDateTime end = (LocalDateTime) stringRedisTemplate.opsForHash().get(stockKey, "end");
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(begin)) {return null;}
        if(now.isAfter(end)) {return null;}
        // 校验一人一单，判断库存并尝试扣减
        String userKey = RedisConstants.SECKILL_USER_KEY+voucherId;
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT,
                Arrays.asList(userKey, stockKey), userId, "stock");
        if(result == 0) return null;
        // 下单成功
        // 修改数据库
        boolean success = seckillVoucherService.update()
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .setSql("stock = stock - 1")
                .update();
        if(!success) {
            log.error("缓存扣减成功，但数据库库存不足");
            return null;
        }
        // 生成订单id
        long orderId = redisIDWorker.nextId(RedisConstants.SECKILL_ORDER_KEY+voucherId);
        // 异步生成订单
        SeckillOrderMessage seckillOrderMessage = new SeckillOrderMessage(
                orderId, voucherId, userId
        );
        rocketMQTemplate.syncSend(MqConstants.SECKILL_ORDER_TOPIC, seckillOrderMessage);
        return orderId;
    }

    @Transactional
    @Override
    public void checkPayment(Long userId, Long voucherId, Long orderId) {
        Integer status = voucherOrderMapper.queryStatus(orderId);
        // 未支付，取消订单
        if(status == 1){
            IVoucherOrderService voucherOrderService = applicationContext.getBean(IVoucherOrderService.class);
            voucherOrderService.cancelOrder(userId, voucherId, orderId);
        }
    }

    /**
     * 未支付而取消订单的接口
     * @param orderId
     * @return
     */
    @Override
    @Transactional
    public boolean cancelOrder(Long userId, Long voucherId, Long orderId) {
        // 操作数据库，增加库存
        boolean success = seckillVoucherService.update()
                .eq("voucher_id", voucherId)
                .setSql("stock = stock + 1")
                .update();
        if(!success) {
            throw new RuntimeException("订单取消错误");
        }
        // 操作缓存（增加库存，取消原本的用户一单记录）
        String stockKey = RedisConstants.SECKILL_STOCK_KEY+voucherId;
        String userKey = RedisConstants.SECKILL_USER_KEY+voucherId;
        stringRedisTemplate.execute(
                SECKILL_CANCEL_SCRIPT,
                Lists.newArrayList(stockKey, userKey),
                "stock", userId
        );
        // 修改订单状态
        success = update().eq("id", orderId)
                .setSql("status = 4")
                .set("update_time", LocalDateTime.now())
                .update();
        if(!success) {
            throw new RuntimeException("订单取消错误!!");
        }
        return true;
    }
}
