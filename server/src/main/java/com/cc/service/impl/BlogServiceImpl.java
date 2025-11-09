package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.constant.RedisConstants;
import com.cc.dto.FeedPushMessage;
import com.cc.dto.UserDTO;
import com.cc.entity.Blog;
import com.cc.entity.User;
import com.cc.mapper.BlogMapper;
import com.cc.mq.FeedPushProducer;
import com.cc.service.IBlogService;
import com.cc.service.IFollowService;
import com.cc.service.IUserInfoService;
import com.cc.service.IUserService;
import com.cc.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private FeedPushProducer feedPushProducer;

    @Override
    public Long publishBlog(Blog blog) {
        // 保存探店博文
        boolean isSuccess = save(blog);
        if(!isSuccess) return null;

        // feed流处理
        // 设置是否异步的阈值
        int asyncThreshold = 100;
        // 设置是否推拉的阈值
        int pullThreshold = 1000;
        // 判断粉丝数
        Long followerCount = userInfoService.queryFollowerCount(blog.getUserId());
        if(followerCount == 0) return blog.getId();
        // 粉丝数较少，使用同步推送模式
        if(followerCount <= asyncThreshold){
            // 查询粉丝
            List<Long> fansIds = followService.queryFansIds(blog.getUserId());
            // 推送博文id到粉丝的收件箱
            for(Long fanId: fansIds){
                String key = RedisConstants.FEED_KEY + fanId;
                stringRedisTemplate.opsForZSet()
                        .add(key, blog.getId().toString(), System.currentTimeMillis());
            }
        }else if(followerCount <= pullThreshold){
            // 粉丝数较多，使用异步推送模式
            FeedPushMessage msg = new FeedPushMessage(blog.getUserId(), blog.getId());
            feedPushProducer.sendPushMessage(msg);
        }else{
            // 粉丝数极多，使用拉取模式，无需推送 todo
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
}
