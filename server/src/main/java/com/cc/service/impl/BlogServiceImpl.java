package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.constant.MqConstants;
import com.cc.constant.RedisConstants;
import com.cc.constant.UserActiveConstant;
import com.cc.constant.UserInfluencerConstants;
import com.cc.dto.BlogPublishDTO;
import com.cc.dto.mq.BlogPublishMessage;
import com.cc.dto.mq.FeedPushMessage;
import com.cc.dto.UserDTO;
import com.cc.entity.Blog;
import com.cc.entity.User;
import com.cc.mapper.BlogMapper;
import com.cc.service.IBlogService;
import com.cc.service.IFollowService;
import com.cc.service.IUserInfoService;
import com.cc.service.IUserService;
import com.cc.utils.UserHolder;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserInfoService userInfoService;
    @Autowired
    private IFollowService followService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private BlogMapper blogMapper;

    /**
     * 发布探店博文
     * @param blogPublishDTO
     * @return
     */
    @Override
    public Long publishBlog(BlogPublishDTO blogPublishDTO) {
        Blog blog = new Blog();
        BeanUtils.copyProperties(blogPublishDTO,blog);
        blog.setUserId(UserHolder.getUserId());

        // 保存探店博文
        boolean isSuccess = save(blog);
        if(!isSuccess) return null;

        // 发送博文推送成功的消息，使agent响应
        BlogPublishMessage publishMessage = new BlogPublishMessage(blog.getId(), MDC.get("traceId"));
        rocketMQTemplate.syncSend(MqConstants.BLOG_PUBLISH_TOPIC, MessageBuilder.withPayload(publishMessage).build());

        // feed流处理
        // 判断影响力，确认是否要推送
        Double influence = stringRedisTemplate.opsForZSet()
                .score(RedisConstants.USER_INFLUENCE_KEY, blog.getUserId().toString());
        if(influence != null && influence > UserInfluencerConstants.INFLUENCER_THRESHOLD) {
            // 拉取模式，维护博文列表
            stringRedisTemplate.opsForList()
                    .rightPush(RedisConstants.FEED_AUTHOR_KEY+blog.getUserId(), blog.getId().toString());
            Long size = stringRedisTemplate.opsForList()
                    .size(RedisConstants.FEED_AUTHOR_KEY+blog.getUserId());
            if(size != null && size > RedisConstants.FEED_AUTHOR_LIST_MAX) {
                stringRedisTemplate.opsForList()
                        .leftPop(RedisConstants.FEED_AUTHOR_KEY+blog.getUserId());
            }
            return blog.getId();
        }

        // 推送模式
        // 设置是否异步的阈值
        int asyncThreshold = 100;
        // 判断粉丝数
        Long followerCount = userInfoService.queryFollowerCount(blog.getUserId());
        if(followerCount == 0) return blog.getId();
        // 粉丝数较少，使用同步推送模式
        if(followerCount <= asyncThreshold){
            // 查询粉丝
            List<Long> fansIds = followService.queryFansIds(blog.getUserId());
            // 推送博文id到粉丝的收件箱
            for(Long fanId: fansIds){
                // 判断是否是活跃用户
                Double score = stringRedisTemplate.opsForZSet()
                        .score(RedisConstants.USER_ACTIVE_KEY,fanId.toString());
                if(score == null || score < UserActiveConstant.ACTIVE_THRESHOLD) continue;
                String key = RedisConstants.FEED_BOX_KEY + fanId;
                stringRedisTemplate.opsForZSet()
                        .add(key, blog.getId().toString(), System.currentTimeMillis());
                // todo 超过一定数量后，移除最早的消息
            }
        }else{
            // 粉丝数较多，使用异步推送模式
            FeedPushMessage msg = new FeedPushMessage(blog.getUserId(), blog.getId(), MDC.get("traceId"));
            rocketMQTemplate.syncSend(MqConstants.FEED_PUSH_TOPIC, MessageBuilder.withPayload(msg).build());
        }
        return blog.getId();
    }

    // 返回isLiked，操作后是否已点赞
    @Override
    public Boolean likeBlog(Long id) {
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        // 获取当前用户
        Long userId = UserHolder.getUserId();
        // 判断是否点赞过
        if(Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, userId.toString()))){
            // 已点赞，取消点赞
            // 修改点赞数
            boolean isSuccess = update().setSql("liked = liked-1").eq("id", id).update();
            if(!isSuccess) return null;
            stringRedisTemplate.opsForZSet().remove(key, userId);
            return false;
        }else{
            // 修改点赞数
            boolean isSuccess = update().setSql("liked = liked+1").eq("id", id).update();
            if(!isSuccess) return null;
            // 按时间排序，所以直接用时间作为score
            stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            return true;
        }
    }

    @Override
    public List<UserDTO> queryBlogLikesTop5(Long id) {
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Set<String> ids = stringRedisTemplate.opsForZSet()
                .range(key, 0, 4);
        if(ids == null || ids.isEmpty()) return new ArrayList<>();
        List<UserDTO> userDTOS = new ArrayList<>();
        for(String userId: ids){
            User user = userService.getById(Long.parseLong(userId));
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user,userDTO);
            userDTOS.add(userDTO);
        }
        return userDTOS;
    }

    @Override
    public List<Blog> queryBlogsByTimeline(Long max, Integer offset) {
        return blogMapper.queryBlogsByTimeline(
                max,
                offset
        );
    }

    @Override
    public List<Blog> queryBlogsByBlogId(List<Long> blogIds) {
        return blogMapper.queryBlogsByBlogId(blogIds);
    }
}
