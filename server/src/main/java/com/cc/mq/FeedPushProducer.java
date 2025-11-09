package com.cc.mq;

import com.cc.constant.MqConstants;
import com.cc.dto.FeedPushMessage;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class FeedPushProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    public void sendPushMessage(FeedPushMessage msg) {
        Message<FeedPushMessage> message = MessageBuilder.withPayload(msg).build();
        rocketMQTemplate.syncSend(MqConstants.FEED_PUSH_TOPIC, message);
    }
}
