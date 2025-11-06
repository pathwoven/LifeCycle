package com.cc.interceptor;

import com.cc.utils.UserHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        if(!(handler instanceof HandlerMethod)){
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }
        // 取出session
        HttpSession session = request.getSession();
        // 获取id
        Long id = (Long) session.getAttribute("userId");
        if(id == null) return false;
        UserHolder.saveUserId(id);
        return true;
    }

}
