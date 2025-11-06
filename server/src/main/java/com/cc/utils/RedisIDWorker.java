package com.cc.utils;

import com.cc.constant.RedisConstants;
import io.lettuce.core.api.sync.RedisAclCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIDWorker {
    private static final long BEGIN_TIMESTAMP = 14432600000L;

    private static final int COUNT_BITS = 32;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String key) {
        // 时间戳
        LocalDateTime now = LocalDateTime.now();
        long timestamp = now.toEpochSecond(ZoneOffset.UTC) - BEGIN_TIMESTAMP;
        // 序列号
        // 获取日期
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment(RedisConstants.ID_INCREMENT_KEY+key+":"+date);
        if(count == null) return -1;
        return timestamp << COUNT_BITS | count;
    }
}
