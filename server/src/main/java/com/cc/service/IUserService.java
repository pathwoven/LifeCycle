package com.cc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.entity.User;
import com.cc.vo.UserLoginVO;

public interface IUserService{
    User loginByPhone(String phone);
    User getById(Long id);
}
