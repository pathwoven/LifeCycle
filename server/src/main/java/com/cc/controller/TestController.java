package com.cc.controller;

import com.cc.dto.Result;
import com.cc.service.ICacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private ICacheService cacheService;

    @GetMapping("/cache/warm-up")
    public Result warmUp(){
        cacheService.warmUpSeckill();
        return Result.ok();
    }
}
