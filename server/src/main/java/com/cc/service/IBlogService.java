package com.cc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.dto.UserDTO;
import com.cc.entity.Blog;

import java.util.List;

public interface IBlogService extends IService<Blog> {
    Long publishBlog(Blog blog);
    Boolean likeBlog(Long id);
    List<UserDTO> queryBlogLikesTop5(Long id);
}
