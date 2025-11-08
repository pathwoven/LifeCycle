package com.cc.mq;

import com.cc.constant.MqConstants;
import com.cc.dto.SeckillOrderMessage;
import com.cc.entity.VoucherOrder;
import com.cc.service.IVoucherOrderService;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RocketMQMessageListener(topic = MqConstants.SECKILL_ORDER_TOPIC, consumerGroup =  MqConstants.SECKILL_ORDER_GROUP)
public class SeckillOrderConsumer implements RocketMQListener<SeckillOrderMessage> {
    @Autowired
    IVoucherOrderService voucherOrderService;
    @Autowired
    RocketMQTemplate rocketMQTemplate;
    @Override
    public void onMessage(SeckillOrderMessage msg) {
        // 幂等性检查
        if(voucherOrderService.getById(msg.getSeckillOrderId()) != null){return;}

        // 生成订单
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(msg.getSeckillOrderId());
        voucherOrder.setCreateTime(LocalDateTime.now());
        voucherOrder.setUpdateTime(LocalDateTime.now());
        voucherOrder.setUserId(msg.getUserId());
        voucherOrder.setVoucherId(msg.getVoucherId());
        voucherOrder.setStatus(1);

        voucherOrderService.save(voucherOrder);

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
    }
}
