package com.cc.interceptor;

import com.cc.properties.JwtProperties;
import com.cc.utils.UserHolder;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import io.jsonwebtoken.Jwts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
@Slf4j
public class UserJwtInterceptor implements HandlerInterceptor {
    @Autowired
    JwtProperties jwtProperties;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        // 判断是否是动态方法
        if(!(handler instanceof HandlerMethod)) return true;

        String token = request.getHeader("Authorization");
        if(token == null) {
            log.debug("token is null，认证失败");
            return false;
        }
        // 取出id
        try {
            // 校验jwt
            Claims claims = Jwts.parser().setSigningKey(jwtProperties.getSecretKey()).parseClaimsJws(token).getBody();
            Integer id = Integer.valueOf(claims.get("userId").toString());
            // 保存id
            UserHolder.saveUserId(id);
        }catch (Exception e){
            log.error(e.getMessage());
            // 返回响应
            response.setStatus(401);
            return false;
        }
        return true;
    }

}
