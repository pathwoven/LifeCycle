package com.cc.mq;

import com.cc.constant.MqConstants;
import com.cc.constant.RedisConstants;
import com.cc.dto.SeckillOrderMessage;
import com.cc.entity.VoucherOrder;
import com.cc.service.ISeckillVoucherService;
import com.cc.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RocketMQMessageListener(topic = MqConstants.SECKILL_ORDER_TOPIC, consumerGroup =  MqConstants.SECKILL_ORDER_GROUP)
public class SeckillOrderConsumer implements RocketMQListener<SeckillOrderMessage> {
    @Autowired
    private IVoucherOrderService voucherOrderService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    // 利用数据库天然幂等性实现分布式排他
    public void onMessage(SeckillOrderMessage msg) {
        String traceId = msg.getTraceId();
        MDC.put("traceId", traceId);
        try {
            // 生成订单
            VoucherOrder voucherOrder = new VoucherOrder();
            voucherOrder.setId(msg.getSeckillOrderId());
            voucherOrder.setCreateTime(LocalDateTime.now());
            voucherOrder.setUpdateTime(LocalDateTime.now());
            voucherOrder.setUserId(msg.getUserId());
            voucherOrder.setVoucherId(msg.getVoucherId());
            voucherOrder.setStatus(1);

            // 幂等性检查 （由于id不可重复，所以假如插入失败，说明已经存在该订单）
            try {
                boolean success = voucherOrderService.save(voucherOrder);
                if (!success) {
                    log.warn("订单已存在，无需重复下单, orderId={}", msg.getSeckillOrderId());
                    return; // 幂等返回
                }
            } catch (DuplicateKeyException e) {
                // note: 记得要捕获异常！防止mq以为消费失败而重试消费
                log.info("订单已存在（DuplicateKey），orderId={}", msg.getSeckillOrderId());
                return;
            }

            // 应该只有唯一的那个插入成功的才能进来，所以应该不用加锁

            // 扣减库存
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", msg.getVoucherId())
                    .gt("stock", 0)
                    .update();
            if (!success) {
                stringRedisTemplate.opsForValue().set(RedisConstants.ORDER_STATUS_KEY + msg.getSeckillOrderId(),
                        "-1", RedisConstants.SECKILL_ORDER_TTL_MIN, TimeUnit.MINUTES);
                log.warn("库存不足，秒杀失败，orderId={}", msg.getSeckillOrderId());
                return;
            }
            stringRedisTemplate.opsForValue().set(RedisConstants.ORDER_STATUS_KEY + msg.getSeckillOrderId(),
                    "1", RedisConstants.SECKILL_ORDER_TTL_MIN, TimeUnit.MINUTES);


            // 发送延时消息以便超时取消
//        rocketMQTemplate.asyncSend(
//                MqConstants.SECKILL_ORDER_TIMEOUT_TOPIC,
//                MessageBuilder.withPayload(msg).build(),
//                new SendCallback() {
//                    @Override
//                    public void onSuccess(SendResult sendResult) {
//
//                    }
//                    @Override
//                    public void onException(Throwable throwable) {
//                        throw new RuntimeException(throwable);
//                    }
//                },
//                3000,
//                16
//        );
            rocketMQTemplate.syncSend(MqConstants.SECKILL_ORDER_TOPIC,
                    MessageBuilder.withPayload(msg).build(),
                    3000, MqConstants.SECKILL_ORDER_DELAY_LEVEL);
        }finally {
            MDC.clear();
        }
    }
}
