package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.constant.RedisConstants;
import com.cc.dto.UserDTO;
import com.cc.entity.Blog;
import com.cc.entity.User;
import com.cc.mapper.BlogMapper;
import com.cc.service.IBlogService;
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
