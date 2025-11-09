package com.cc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.entity.Follow;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FollowMapper extends BaseMapper<Follow> {

    @Select("select user_id from tb_follow where follow_user_id = #{userId}")
    List<Long> queryFansIds(Long userId);

}
