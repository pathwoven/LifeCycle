package com.cc.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cc.constant.RedisConstants;
import com.cc.dto.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@Slf4j
public class CacheClient {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * String类型
     * @param key
     * @param value 接受对象为参数，会自动序列化为json
     * @param expireTime
     * @param timeUnit
     */
    public void setWithExpire(String key,Object value, Long expireTime, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), expireTime, timeUnit);
    }

    public void setWithLogicalExpire(String key, Object value, Long expireTime, TimeUnit timeUnit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        LocalDateTime expire = LocalDateTime.now().plusSeconds(timeUnit.toSeconds(expireTime));
        redisData.setExpireTime(expire);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 返回null表示未查询到对象
     * @param keyPrefix
     * @param id
     * @param type value的类型
     * @param dbFallback 数据库的操作回调
     * @param expireTime
     * @param timeUnit
     * @return
     * @param <R>
     * @param <ID>
     */
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallback,
                                          Long expireTime, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        String jsonValue = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(jsonValue)) {
            return JSONUtil.toBean(jsonValue, type);
        }
        if(jsonValue != null) return null;
        R r = dbFallback.apply(id);
        if(r==null){
            // 设置空值
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL_MIN, TimeUnit.MINUTES);
            return null;
        }
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(r), expireTime, timeUnit);
        return r;
    }

    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type,
                                            Function<ID, R> dbFallback,Long expireTime, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        String jsonValue = stringRedisTemplate.opsForValue().get(key);
        if(jsonValue != null && StrUtil.isBlank(jsonValue)) {
            return null;
        }
        if(jsonValue == null){
            // 查询数据
            R r = dbFallback.apply(id);
            if(r==null){
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL_MIN, TimeUnit.MINUTES);
            }else{
                setWithLogicalExpire(key,r,expireTime,timeUnit);
            }
            return r;
        }
        RedisData redisData = JSONUtil.toBean(jsonValue, RedisData.class);
        // 判断是否过期
        if(LocalDateTime.now().isBefore(redisData.getExpireTime())){
            return (R)redisData.getData();
        }
        if(tryLock(key)){
            R r = dbFallback.apply(id);
            if(r==null){
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL_MIN, TimeUnit.MINUTES);
            }else{
                setWithLogicalExpire(key,r,expireTime,timeUnit);
            }
            unlock(key);
            return r;
        }
        return (R)redisData.getData();
    }

    public <R, ID> R queryWithMutex(String keyPrefix, ID id,
            Class<R> type, Function<ID, R> dbFallback, Long expireTime, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        String jsonValue = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(jsonValue)) {
            return JSONUtil.toBean(jsonValue, type);
        }
        if(jsonValue != null) return null;
        // 尝试获取锁
        if(tryLock(key)){
            // 查询数据库
            R r = dbFallback.apply(id);
            if(r==null){
                stringRedisTemplate.opsForValue().set(key, "",
                        RedisConstants.CACHE_NULL_TTL_MIN, TimeUnit.MINUTES);
            }else{
                stringRedisTemplate.opsForValue().set(key,
                        JSONUtil.toJsonStr(r), expireTime, timeUnit);
            }
            unlock(key);
            return r;
        }else{
            try{
                Thread.sleep(50);
            }catch(InterruptedException e){
                throw new RuntimeException(e);
            }
            return queryWithMutex(keyPrefix, id, type, dbFallback, expireTime, timeUnit);
        }
    }

    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue()
                .setIfAbsent(RedisConstants.LOCK_PREFIX_KEY+key, "1"
                        , RedisConstants.LOCK_TTL_SECOND, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    // 只是用来删锁，未保证锁的所属权
    private void unlock(String key){
        stringRedisTemplate.delete(RedisConstants.LOCK_PREFIX_KEY+key);
    }
}
