package com.cc.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.dto.UserRegisterDto;
import com.cc.entity.User;

public interface IUserService extends IService<User> {
    User loginByPhone(String phone);

    boolean register(UserRegisterDto userRegisterDto);

}
