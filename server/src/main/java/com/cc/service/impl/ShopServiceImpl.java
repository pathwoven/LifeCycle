package com.cc.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.constant.RedisConstants;
import com.cc.entity.Shop;
import com.cc.mapper.ShopMapper;
import com.cc.service.IShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     *  根据id查询商铺，带互斥锁防止缓存击穿，同时，对数据库没查到的数据设置空对象，防止缓存击穿
     * @param id
     * @return
     */
    @Override
    public Shop queryShopByIdWithMutex(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(shopJson)) {
            // 存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 注意：这里要判断非null!!
        // 如果为null,表示没查到缓存，那么就可以之后的更新缓存
        // 而如果不为空，同时不是isNotBlank，证明是一个空对象，是防止缓存击穿设置的空对象
        if(shopJson != null) return null;
        // 查询数据库
        // 先获取锁
        if (BooleanUtil.isFalse(
                stringRedisTemplate.opsForValue().setIfAbsent(
                        RedisConstants.LOCK_SHOP_KEY + id,
                        "1", RedisConstants.LOCK_TTL_SECOND, TimeUnit.SECONDS))) {
            // 未取得锁，休眠
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 再次尝试
            queryShopByIdWithMutex(id);
        }
        Shop shop = getById(id);
        if (shop == null) {
            // 释放锁
            stringRedisTemplate.delete(RedisConstants.LOCK_SHOP_KEY + id);
            // 设置空对象
            stringRedisTemplate.opsForValue().set(key, "");
            return null;
        }
        // 写入缓存
        stringRedisTemplate.opsForValue().set(key,
                JSONUtil.toJsonStr(shop),
                RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        // 释放锁
        stringRedisTemplate.delete(RedisConstants.LOCK_SHOP_KEY + id);
        return shop;
    }

    @Override
    public Shop queryShopByIdWithLogicExpiration(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 查询到了，且为空字符串，则直接返回null（防缓存击穿）
        if(shopJson != null &&StrUtil.isBlank(shopJson)) return null;
        // 判断是否过期

        // 查询数据库
        // 尝试获取锁
        if(BooleanUtil.isFalse(
                stringRedisTemplate.opsForValue().setIfAbsent(RedisConstants.LOCK_SHOP_KEY+id,
                        "1", RedisConstants.LOCK_TTL_SECOND, TimeUnit.SECONDS)
        )){
            // 未获取到
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {throw new RuntimeException(e);}
            // 直接返回旧数据
        }
        // 获取到锁

        return null;
    }
}
