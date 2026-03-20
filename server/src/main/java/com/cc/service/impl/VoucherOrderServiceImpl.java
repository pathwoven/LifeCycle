package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.constant.MqConstants;
import com.cc.constant.RedisConstants;
import com.cc.dto.OrderDTO;
import com.cc.dto.mq.SeckillOrderMessage;
import com.cc.entity.Payment;
import com.cc.entity.VoucherOrder;
import com.cc.mapper.VoucherOrderMapper;
import com.cc.service.IPaymentService;
import com.cc.service.ISeckillVoucherService;
import com.cc.service.IVoucherOrderService;
import com.cc.utils.Alipay;
import com.cc.utils.RedisIDWorker;
import com.google.common.collect.Lists;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private IPaymentService paymentService;
    @Autowired
    private Alipay alipay;
    @Autowired
    private RedissonClient redissonClient;


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
//        // 修改数据库
//        boolean success = seckillVoucherService.update()
//                .eq("voucher_id", voucherId)
//                .gt("stock", 0)
//                .setSql("stock = stock - 1")
//                .update();
//        if(!success) {
//            log.error("缓存扣减成功，但数据库库存不足");
//            return null;
//        }
        // 生成订单id
        long orderId = redisIDWorker.nextId(RedisConstants.ID_ORDER_KEY);
        // 异步生成订单
        SeckillOrderMessage seckillOrderMessage = new SeckillOrderMessage(
                orderId, voucherId, userId, MDC.get("traceId")
        );
        // todo 同步改异步，并增加本地补偿表
        rocketMQTemplate.syncSend(MqConstants.SECKILL_ORDER_TOPIC, seckillOrderMessage);

        stringRedisTemplate.opsForValue()
                .set(RedisConstants.ORDER_STATUS_KEY +orderId, "0", RedisConstants.SECKILL_ORDER_TTL_MIN, TimeUnit.MINUTES);
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

    /**
     * 在redis中获取支付链接，假如没有，则获取锁、生成并写入redis
     * @param orderId
     * @param type
     * @return
     */
    @Override
    public String getPayLink(Long orderId, Integer type) {
        // 从redis中获取
        String key = RedisConstants.PAY_LINK_KEY + orderId + ":" + type;
        String url = stringRedisTemplate.opsForValue().get(key);
        if(url != null){
            return url;
        }
        // 从数据库中获取
        // 加锁
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_PAY_KEY + orderId + ":" + type);
        boolean isLock = lock.tryLock();
        if(!isLock) return null;   // 让另一个线程处理
        try {
            // 再次检查redis与数据库是否有相应记录
            url = stringRedisTemplate.opsForValue().get(key);
            if (url != null) {
                return url;
            }
            // 数据库中查询
            // 订单过期时间与支付的过期时间相同，所以同一orderId与type的支付链接只会对应唯一一条记录
            Payment payment = paymentService.query()
                    .eq("order_id", orderId)
                    .eq("type", type)
                    .one();
            if (payment != null) {
                if (payment.getStatus() != 1) {
                    return null;
                }
                return payment.getPayUrl();
            }
            // redis和数据库中都没有
            // 调用第三方支付接口生成支付链接 todo
            Long outTradeNo = redisIDWorker.nextId(RedisConstants.ID_OUT_TRADE_KEY);
            VoucherOrder voucherOrder = voucherOrderMapper.selectById(orderId);
            LocalDateTime expireTime = voucherOrder.getCreateTime().plusMinutes(RedisConstants.PAY_LINK_TTL_MIN);
            url = alipay.createWapPay(new OrderDTO(outTradeNo,
                    voucherOrder.getAmount(), "Voucher Order Payment", "example.com", expireTime));  // todo

            // 写入数据库
            Payment newPayment = new Payment();
            newPayment.setOrderId(orderId);
            newPayment.setChannel(type);
            newPayment.setStatus(1);
            newPayment.setCreateTime(LocalDateTime.now());
            newPayment.setUpdateTime(LocalDateTime.now());
            newPayment.setPayUrl(url);
            newPayment.setExpireTime(expireTime);
            newPayment.setAmount(voucherOrder.getAmount());
            newPayment.setOutTradeNo(outTradeNo);
            // 写入redis
            stringRedisTemplate.opsForValue()
                    .set(key, url, RedisConstants.PAY_LINK_TTL_MIN, TimeUnit.MINUTES);
        }finally {
            // 释放锁
            lock.unlock();
        }

        return url;
    }

    /**
     * 获取订单的状态
     * @param orderId
     * @return
     */
    @Override
    public Integer getStatus(Long orderId) {
        // 先redis中查询
        String s = stringRedisTemplate.opsForValue().get(RedisConstants.ORDER_STATUS_KEY + orderId);
        if(s != null){
            return Integer.valueOf(s);
        }
        // 数据库中查询
        return voucherOrderMapper.queryStatus(orderId);
    }

    @Transactional
    @Override
    public boolean alipayPayCallback(Map<String, String> params) {
        // 验签，并验证app-id与seller-id
        boolean success = alipay.rsaVerify(params);
        if(!success) return false;
        // 验签成功，进行参数二次校验
        String outTradeNoStr = params.get("out_trade_no");
        Long outTradeNo = Long.valueOf(outTradeNoStr);
        String tradeStatus = params.get("trade_status");
        BigDecimal totalAmount = new BigDecimal(params.get("total_amount"));
        long amountFen = totalAmount.multiply(new BigDecimal(100)).longValue();
        // 确认状态为成功
        if(!tradeStatus.equals("TRADE_SUCCESS") && !tradeStatus.equals("TRADE_FINISHED")) {
            return false;
        }
        // 查询支付单
        Payment payment = paymentService.query()
                .eq("out_trade_no", outTradeNo)
                .eq("channel", 1)
                .one();
        // 校验订单号和金额
        if(payment == null || payment.getStatus() == -1 || payment.getAmount() != amountFen) {
            log.error("支付宝异常回调单号，第三方单号：" + outTradeNoStr);
            return false;
        }
        success = paymentService.update().eq("out_trade_no", outTradeNo)
                .eq("status", 1)
                .eq("channel", 1)
                .set("status", 2)
                .set("update_time", LocalDateTime.now())
                .update();
        // 已经修改过该支付单了
        if(!success) {
            return true;
        }
        // 修改订单状态（自然幂等）
        Integer row = voucherOrderMapper.updateStatusSuccess(payment.getOrderId(), payment.getChannel());
        if(row != 1) {
            log.error("支付宝回调时，订单状态异常，orderId=" + payment.getOrderId());
        }

        // 修改redis上状态
        stringRedisTemplate.opsForValue().set(RedisConstants.ORDER_STATUS_KEY +payment.getOrderId(),
                "2", RedisConstants.SECKILL_ORDER_TTL_MIN, TimeUnit.MINUTES);

        return true;
    }
}
