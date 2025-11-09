package com.cc.controller;


import com.cc.dto.Result;
import com.cc.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/follow")
public class FollowController {
    @Autowired
    private IFollowService followService;

    /**
     * 关注/取关用户
     * 返回操作后是否已关注
     */
    @RequestMapping("/{id}")
    public Result follow(@PathVariable Long id){
        Boolean isFollow = followService.follow(id);
        if(isFollow == null){
            return Result.fail("操作失败");
        }
        return Result.ok(isFollow);
    }
}
