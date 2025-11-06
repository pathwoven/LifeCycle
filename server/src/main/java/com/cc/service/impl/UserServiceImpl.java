package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.entity.User;
import com.cc.mapper.UserMapper;
import com.cc.properties.JwtProperties;
import com.cc.service.IUserService;
import com.cc.vo.UserLoginVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public User loginByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    public User getById(Long id) {
        return null;
    }
}
