package com.cc.controller;

import com.cc.constant.RedisConstants;
import com.cc.dto.FeedBlogDTO;
import com.cc.dto.Result;
import com.cc.entity.Blog;
import com.cc.entity.User;
import com.cc.service.IBlogService;
import com.cc.service.IUserInfoService;
import com.cc.service.IUserService;
import com.cc.utils.UserHolder;
import com.cc.vo.FeedCursorVO;
import com.cc.vo.FeedHomeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/feed")
@Slf4j
public class FeedController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private IBlogService blogService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserInfoService userInfoService;
    @GetMapping("/blog/follow/{cursor}")
    public Result feedBlog(@PathVariable Double cursor) {
        FeedCursorVO feedCursorVO = getFeedFollowVO(cursor, RedisConstants.FEED_PAGE_SIZE, false);
        return Result.ok();
    }

    /**
     *
     * @param cursor
     * @param pageSize
     * @param exRead 是否要排除已读的，并且将未读的标记为已读
     * @return
     */
    private FeedCursorVO getFeedFollowVO(Double cursor, Integer pageSize, boolean exRead) {
        if(cursor == null || cursor < 0) {
            // 第一次获取
            cursor = (double) System.currentTimeMillis();
        }
        Set<TypedTuple<String>> entries = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(RedisConstants.FEED_BOX_KEY+ UserHolder.getUserId(),
                        -1, cursor, 0, pageSize);
        if(entries == null || entries.isEmpty()) {
            FeedCursorVO feedCursorVO = new FeedCursorVO();
            // 从数据库拉取
            FeedBlogDTO feedBlogDTO = new FeedBlogDTO();
            List<Blog> blogList = blogService.queryBlogsByTimeline(cursor.longValue(), pageSize);
            feedCursorVO.setCursor(cursor);
            feedCursorVO.setFeedBlogDTOList(
                    blogList.stream()
                            .filter(blog -> {
                                if(exRead) {
                                    // 排除已读的
                                    Double score = stringRedisTemplate.opsForZSet()
                                            .score(RedisConstants.FEED_REED_KEY + UserHolder.getUserId(),
                                                    blog.getId().toString());
                                    if(score == null) {
                                        // 标记为已读
                                        stringRedisTemplate.opsForZSet().add(
                                                RedisConstants.FEED_REED_KEY + UserHolder.getUserId(),
                                                blog.getId().toString(),
                                                System.currentTimeMillis()
                                        );
                                    }
                                    return score == null;
                                }
                                return true;
                            })
                            .map(blog -> {
                                FeedBlogDTO dto = new FeedBlogDTO();
                                BeanUtils.copyProperties(blog, dto);
                                dto.setBlogId(blog.getId());
                                // 设置作者信息
                                User user  = userService.getById(dto.getUserId());
                                dto.setUserName(user.getNickName());
                                dto.setUserIcon(user.getIcon());
                                return dto;
                            }).collect(Collectors.toList())
            );
            return feedCursorVO;
        }
        return getFeedsByZSetEntries(exRead, entries);
    }

    private FeedCursorVO getFeedsByZSetEntries(boolean exRead, Set<TypedTuple<String>> entries) {
        FeedCursorVO feedCursorVO = new FeedCursorVO();
        Double cursor = entries.stream().min((o1, o2) -> {
            if(o1.getScore() == null || o2.getScore() == null) {
                return 0;
            }
            return o1.getScore().compareTo(o2.getScore());
        }).get().getScore();
        feedCursorVO.setCursor(cursor);
        // 根据id查询blog
        List<Long> blogIds = entries.stream()
                .map(TypedTuple::getValue)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        List<FeedBlogDTO> feedBlogDTOList = new ArrayList<>();
        for (Long blogId : blogIds) {
            if(exRead) {
                // 排除已读的
                Double score = stringRedisTemplate.opsForZSet()
                        .score(RedisConstants.FEED_REED_KEY + UserHolder.getUserId(),
                                blogId.toString());
                if(score == null) {
                    // 标记为已读
                    stringRedisTemplate.opsForZSet().add(
                            RedisConstants.FEED_REED_KEY + UserHolder.getUserId(),
                            blogId.toString(),
                            System.currentTimeMillis()
                    );
                } else {
                    continue;
                }
            }
            Blog blog = blogService.getById(blogId);
            if (blog == null) {
                continue;
            }
            FeedBlogDTO dto = new FeedBlogDTO();
            BeanUtils.copyProperties(blog, dto);
            dto.setBlogId(blog.getId());
            // 设置作者信息
            User user = userService.getById(dto.getUserId());
            dto.setUserName(user.getNickName());
            dto.setUserIcon(user.getIcon());
            feedBlogDTOList.add(dto);
        }
//        if(feedBlogDTOList.isEmpty()) {
//            // 递归获取下一页
//            return getFeedFollowVO(cursor, pageSize, exRead);
//        }
        feedCursorVO.setFeedBlogDTOList(feedBlogDTOList);
        return feedCursorVO;
    }

    private FeedCursorVO getFeedHotVO(Double cursor, Integer pageSize, String city) {
        if (cursor == null || cursor < 0) {
            // 第一次获取
            cursor = (double) System.currentTimeMillis();
        }
        Set<TypedTuple<String>> entries = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(RedisConstants.BLOG_HOT_CITY_KEY + city,
                        -1, cursor, 0, pageSize);
        FeedCursorVO feedCursorVO = new FeedCursorVO();
        if (entries == null || entries.isEmpty()) {
            return new FeedCursorVO();
        }
        return getFeedsByZSetEntries(true, entries);
    }

    @GetMapping("/blog/home/{followCursor}/{city}/{hotCursor}")
    public Result feedHome(@PathVariable Double followCursor, @PathVariable String city,@PathVariable Double hotCursor) {
        FeedCursorVO feedFollowVO = getFeedFollowVO(followCursor, RedisConstants.FEED_PAGE_FOLLOW_SIZE, true);
        FeedCursorVO feedHotVO = getFeedHotVO(hotCursor,
                RedisConstants.FEED_PAGE_SIZE - RedisConstants.FEED_PAGE_FOLLOW_SIZE, city);
        FeedHomeVO feedHomeVO = new FeedHomeVO();
        feedHomeVO.setFollowCursor(feedFollowVO.getCursor());
        feedHomeVO.setHotCursor(feedHotVO.getCursor());
        List<FeedBlogDTO> combinedList = new ArrayList<>();
        if (feedFollowVO.getFeedBlogDTOList() != null) {
            combinedList.addAll(feedFollowVO.getFeedBlogDTOList());
        }
        if (feedHotVO.getFeedBlogDTOList() != null) {
            combinedList.addAll(feedHotVO.getFeedBlogDTOList());
        }
        feedHomeVO.setFeedBlogDTOList(combinedList);
        return Result.ok(feedHomeVO);
    }
}
