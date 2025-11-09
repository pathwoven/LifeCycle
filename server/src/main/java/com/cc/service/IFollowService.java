package com.cc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.entity.Follow;

import java.util.List;

public interface IFollowService extends IService<Follow> {

    List<Long> queryFansIds(Long userId);

    Boolean follow(Long id);
}
