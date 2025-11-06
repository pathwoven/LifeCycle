package com.cc.utils;

import cn.hutool.core.util.BooleanUtil;
import com.cc.constant.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {
    private final String IDPrefix = UUID.randomUUID().toString()+"-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>();
    static {
        UNLOCK_SCRIPT.setLocation(new ClassPathResource(Paths.get("lua", "unlock.lua").toString()));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public boolean tryLock(String key, Long expireTimeSec) {
        String lockKey = IDPrefix + key;
        String id = IDPrefix + Thread.currentThread().getId();
        return BooleanUtil.isTrue(
                stringRedisTemplate.opsForValue().setIfAbsent(lockKey, id, expireTimeSec, TimeUnit.SECONDS));
    }

    public boolean unlock(String key){
        String lockKey = RedisConstants.LOCK_PREFIX_KEY+key;
        String id = IDPrefix+Thread.currentThread().getId();
        Long result = stringRedisTemplate.execute(UNLOCK_SCRIPT,
                Collections.singletonList(lockKey), id);
        return result == 1;
    }
}
