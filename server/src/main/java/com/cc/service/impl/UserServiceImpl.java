package com.cc.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.dto.UserRegisterDto;
import com.cc.entity.User;
import com.cc.entity.UserInfo;
import com.cc.mapper.UserInfoMapper;
import com.cc.mapper.UserMapper;
import com.cc.service.IUserService;
import com.cc.utils.PasswordEncoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired UserInfoMapper userInfoMapper;

    @Override
    public User loginByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    @Transactional
    public boolean register(UserRegisterDto userRegisterDto) {
        // todo 应该要加锁的
        User user = userMapper.findByPhone(userRegisterDto.getPhone());
        if(user != null) {
            return false;
        }
        user = new User();
        BeanUtils.copyProperties(userRegisterDto,user);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setPassword(PasswordEncoder.encode(user.getPassword()));
        save(user);

        // 创建用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setCreateTime(now);
        userInfo.setUpdateTime(now);
        userInfoMapper.insert(userInfo);
        return true;
    }

}
