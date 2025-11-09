package com.cc.config;

import com.cc.interceptor.MerchantJwtInterceptor;
import com.cc.interceptor.UserJwtInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {
    @Autowired
    // SessionInterceptor sessionInterceptor;
    private UserJwtInterceptor userJwtInterceptor;
    @Autowired
    private MerchantJwtInterceptor merchantJwtInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(userJwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/code")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/register");
        registry.addInterceptor(merchantJwtInterceptor)
                .addPathPatterns("/merchant/**")
                .excludePathPatterns("/merchant/login")
                .excludePathPatterns("/merchant/register");
    }
}
