package com.cc.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.dto.Result;
import com.cc.dto.UserDTO;
import com.cc.entity.Blog;
import com.cc.entity.User;
import com.cc.service.IBlogService;
import com.cc.service.IUserService;
import com.cc.constant.SystemConstants;
import com.cc.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/blog")
@Slf4j
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private IUserService userService;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        blog.setUserId(UserHolder.getUserId());
        // 保存探店博文
        Long id = blogService.publishBlog(blog);
        // 返回id
        return Result.ok(id);
    }

    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        Boolean result = blogService.likeBlog(id);
        if(result == null) return Result.fail("操作失败");
        return Result.ok(result);
    }
     @GetMapping("/likes/{id}")
     public Result getBlogLikesTop5(@PathVariable("id") Long id) {
        //List<UserDTO> users = blogService.queryBlogLikes(id);
         return Result.fail("功能未实现");
     }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", UserHolder.getUserId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            Long userId = blog.getUserId();
            User user = userService.getById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
        });
        return Result.ok(records);
    }

    @GetMapping("/feed")
    public Result queryFeedBlog(){
        return Result.fail("功能未实现");
    }
}
