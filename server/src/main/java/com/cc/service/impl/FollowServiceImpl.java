package com.cc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.constant.RedisConstants;
import com.cc.constant.UserInfluencerConstants;
import com.cc.entity.Follow;
import com.cc.mapper.FollowMapper;
import com.cc.service.IFollowService;
import com.cc.service.IUserInfoService;
import com.cc.service.IUserService;
import com.cc.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private IUserInfoService userInfoService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<Long> queryFansIds(Long userId) {
        return followMapper.queryFansIds(userId);
    }

    @Override
    @Transactional
    public Boolean follow(Long id) {
        Long userId = UserHolder.getUserId();
        if(userId.equals(id)){
            return null;
        }
        // 操作follow表
        Follow follow = followMapper.selectOne(
                new QueryWrapper<Follow>()
                        .eq("user_id", userId)
                        .eq("follow_user_id", id)
                );
        if(follow == null){
            // 关注
            Follow newFollow = new Follow();
            newFollow.setUserId(userId);
            newFollow.setFollowUserId(id);
            FollowServiceImpl followService = applicationContext.getBean(FollowServiceImpl.class);
            boolean isSuccess = followService.save(newFollow);
            if(isSuccess){
                // 更新被关注者的粉丝数+1
                userInfoService.update()
                        .setSql("fans = fans + 1")
                        .eq("user_id", id)
                        .update();
                userInfoService.update()
                        .setSql("followee = followee + 1")
                        .eq("user_id", userId)
                        .update();
                // 维护关注列表
                stringRedisTemplate.opsForSet().add(RedisConstants.USER_FOLLOW_KEY+userId,id.toString());
                // 维护被关注者影响力
                stringRedisTemplate.opsForZSet().add(RedisConstants.USER_INFLUENCE_KEY,
                        id.toString(),
                        UserInfluencerConstants.FANS_ADD);
                return true;
            }else{
                throw new RuntimeException("关注失败");
            }
        }else{
            // 取关
            FollowServiceImpl followService = applicationContext.getBean(FollowServiceImpl.class);
            boolean isSuccess = followService.removeById(follow.getId());
            if(isSuccess){
                // 更新被关注者的粉丝数-1
                userInfoService.update()
                        .setSql("fans = fans - 1")
                        .eq("user_id", id)
                        .update();
                userInfoService.update()
                        .setSql("followee = followee - 1")
                        .eq("user_id", userId)
                        .update();
                // 维护关注列表
                stringRedisTemplate.opsForSet().remove(RedisConstants.USER_FOLLOW_KEY+userId
                        ,id.toString());
                // 维护被关注者影响力
                stringRedisTemplate.opsForZSet().add(RedisConstants.USER_INFLUENCE_KEY,
                        id.toString(),
                        -UserInfluencerConstants.FANS_ADD);
                return false;
            }else{
                throw new RuntimeException("取关失败");
            }
        }
    }

}
