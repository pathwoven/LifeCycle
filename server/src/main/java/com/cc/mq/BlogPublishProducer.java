//package com.cc.mq;
//
//import org.apache.rocketmq.spring.core.RocketMQTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class BlogPublishProducer {
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;
//
//    public void sendBlogPublishMessage(Long blogId) {
//        rocketMQTemplate.asyncSend();
//    }
//}
