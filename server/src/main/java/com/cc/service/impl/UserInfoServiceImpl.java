package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.entity.UserInfo;
import com.cc.mapper.UserInfoMapper;
import com.cc.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Override
    public Long queryFollowerCount(Long userId) {
        return userInfoMapper.queryFollowerCount(userId);
    }
}
