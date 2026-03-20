package com.cc.ai.mq;

import com.cc.ai.service.BlogAiService;
import com.cc.constant.MqConstants;
import com.cc.dto.mq.BlogPublishMessage;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RocketMQMessageListener(topic = MqConstants.BLOG_PUBLISH_TOPIC, consumerGroup = MqConstants.BLOG_PUBLISH_GROUP)
public class BlogPublishConsumer implements RocketMQListener<BlogPublishMessage> {
    @Autowired
    private BlogAiService blogAiService;

    @Override
    public void onMessage(BlogPublishMessage msg) {
        Long blogId = msg.getId();
        String traceId = msg.getTraceId();
        MDC.put("traceId", traceId);
        // 调用agent进行处理
        blogAiService.processBlogGeneration(blogId);
    }
}
