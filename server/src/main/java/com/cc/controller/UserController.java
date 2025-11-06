package com.cc.controller;


import com.cc.dto.LoginFormDTO;
import com.cc.dto.Result;
import com.cc.dto.UserDTO;
import com.cc.entity.User;
import com.cc.entity.UserInfo;
import com.cc.properties.JwtProperties;
import com.cc.service.IUserInfoService;
import com.cc.service.IUserService;
import com.cc.vo.UserLoginVO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // TODO 发送短信验证码并保存验证码
        return Result.fail("功能未完成");
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm){
        User user = userService.loginByPhone(loginForm.getPhone());
        if(user == null){ return Result.fail("手机或密码错误");}
        // 验证密码
        String password = DigestUtils.md5DigestAsHex(loginForm.getPassword().getBytes());
        if(!password.equals(user.getPassword())) return Result.fail("手机或密码错误");

        Date exp = new Date(System.currentTimeMillis() + jwtProperties.getTtl());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        // jwt签发
        String token = Jwts.builder()
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .setClaims(claims).compact();

        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user,userLoginVO);
        userLoginVO.setToken(token);

        return Result.ok(userLoginVO);
    }

    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(){
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    @GetMapping("/me")
    public Result me(){
        // TODO 获取当前登录的用户并返回
        return Result.fail("功能未完成");
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }
}
