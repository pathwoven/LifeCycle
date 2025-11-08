package com.cc.controller;

import com.cc.dto.LoginFormDTO;
import com.cc.dto.Result;
import com.cc.entity.Merchant;
import com.cc.properties.JwtProperties;
import com.cc.service.IMerchantService;
import com.cc.vo.LoginVO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 商户登录
 */
@RestController
@Slf4j
@RequestMapping("/merchant")
public class MerchantController {
    @Autowired
    private IMerchantService merchantService;
    @Autowired
    private JwtProperties jwtProperties;
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO) {
        Merchant merchant = merchantService.login(loginFormDTO);
        if(merchant == null){
            return Result.fail("账号或密码错误");
        }
        // 签发jwt
        Date exp = new Date(System.currentTimeMillis() + jwtProperties.getTtl());
        Map<String, Object> claims = new HashMap<>();
        claims.put("merchantId", merchant.getId());
        String token = Jwts.builder()
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .setClaims(claims)
                .compact();
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(merchant, loginVO);
        loginVO.setToken(token);
        return Result.ok(loginVO);
    }
}
