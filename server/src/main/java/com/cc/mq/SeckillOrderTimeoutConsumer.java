package com.cc.mq;

import com.cc.constant.MqConstants;
import com.cc.dto.SeckillOrderMessage;
import com.cc.service.IVoucherOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 当订单超过时限时，检查是否已完成支付
 */
@Service
@RocketMQMessageListener(topic = MqConstants.SECKILL_ORDER_TIMEOUT_TOPIC, consumerGroup =  MqConstants.SECKILL_ORDER_TIMEOUT_GROUP)
public class SeckillOrderTimeoutConsumer implements RocketMQListener<SeckillOrderMessage> {
    @Autowired
    private IVoucherOrderService voucherOrderService;
    @Override
    public void onMessage(SeckillOrderMessage message) {
        // 检查是否已支付
        voucherOrderService.checkPayment(
                message.getUserId(), message.getVoucherId(), message.getSeckillOrderId()
        );
    }
}
