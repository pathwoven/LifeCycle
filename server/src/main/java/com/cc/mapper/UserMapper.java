package com.cc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper{
    @Select("select * from tb_user where phone=#{phone}")
    User findByPhone(String phone);
}
