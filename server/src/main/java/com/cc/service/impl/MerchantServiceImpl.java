package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.dto.LoginFormDTO;
import com.cc.entity.Merchant;
import com.cc.mapper.MerchantMapper;
import com.cc.service.IMerchantService;
import com.cc.utils.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements IMerchantService {
    @Autowired
    private MerchantMapper merchantMapper;
    @Override
    public Merchant login(LoginFormDTO loginFormDTO) {
        Merchant merchant = merchantMapper.findByPhone(loginFormDTO.getPhone());
        if(merchant == null){
            return  null;
        }
        boolean isEqual = PasswordEncoder.matches(merchant.getPassword(),loginFormDTO.getPassword());
        if(!isEqual){return null;}
        return merchant;
    }
}
