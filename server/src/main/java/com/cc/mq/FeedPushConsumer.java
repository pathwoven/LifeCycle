package com.cc.mq;

import com.cc.constant.MqConstants;
import com.cc.constant.RedisConstants;
import com.cc.constant.UserActiveConstant;
import com.cc.dto.FeedPushMessage;
import com.cc.service.IFollowService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RocketMQMessageListener(topic = MqConstants.FEED_PUSH_TOPIC, consumerGroup = MqConstants.FEED_PUSH_GROUP)
public class FeedPushConsumer implements RocketMQListener<FeedPushMessage> {
    @Autowired
    private IFollowService followService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void onMessage(FeedPushMessage message) {
        double now = System.currentTimeMillis();
        // 写入box
        // 获取粉丝列表
        List<Long> fans = followService.queryFansIds(message.getUserId());
        // 推送消息
        for(Long id : fans){
            // 判断是否是活跃用户
            Double score = stringRedisTemplate.opsForZSet().score(RedisConstants.USER_ACTIVE_KEY,id);
            if(score == null || score < UserActiveConstant.ACTIVE_THRESHOLD){continue;}
            stringRedisTemplate.opsForZSet()
                    .add(RedisConstants.FEED_BOX_KEY+id,
                            message.getBlogId().toString(),
                            now);
//            stringRedisTemplate.expire(RedisConstants.FEED_BOX_KEY+id,
//                    RedisConstants.FEED_BOX_TTL_DAYS,
//                    java.util.concurrent.TimeUnit.DAYS);
            // todo 超过一定数量后，移除最早的消息
        }
    }
}
