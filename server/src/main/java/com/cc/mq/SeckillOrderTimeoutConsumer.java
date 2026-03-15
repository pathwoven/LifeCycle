package com.cc.mq;

import com.cc.constant.MqConstants;
import com.cc.dto.SeckillOrderMessage;
import com.cc.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 当订单超过时限时，检查是否已完成支付
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = MqConstants.SECKILL_ORDER_TIMEOUT_TOPIC, consumerGroup =  MqConstants.SECKILL_ORDER_TIMEOUT_GROUP)
public class SeckillOrderTimeoutConsumer implements RocketMQListener<SeckillOrderMessage> {
    @Autowired
    private IVoucherOrderService voucherOrderService;
    @Override
    public void onMessage(SeckillOrderMessage message) {
        // 检查是否已支付
        // todo 改成从payment表查询，同时未支付的话，要先调用查询接口，确定确实未支付才行
        voucherOrderService.checkPayment(
                message.getUserId(), message.getVoucherId(), message.getSeckillOrderId()
        );
    }
}
