package com.cc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.entity.UserInfo;

public interface IUserInfoService extends IService<UserInfo> {

    Long queryFollowerCount(Long userId);
}
