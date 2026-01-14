package com.cc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.entity.Blog;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BlogMapper extends BaseMapper<Blog> {

    @Select("select * from blog where create_time < #{max} order by id desc limit #{offset}")
    List<Blog> queryBlogsByTimeline(Long max, Integer offset);

    List<Blog> queryBlogsByBlogId(List<Long> blogIds);
}
