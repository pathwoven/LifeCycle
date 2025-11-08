package com.cc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.dto.LoginFormDTO;
import com.cc.entity.Merchant;

public interface IMerchantService extends IService<Merchant> {
    Merchant login(LoginFormDTO loginFormDTO);
}
