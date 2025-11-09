package com.cc.controller;

import com.cc.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
@Slf4j
public class FeedController {
    @GetMapping("/blog")
    public Result feedBlog() {
        return Result.fail("");
    }
}
