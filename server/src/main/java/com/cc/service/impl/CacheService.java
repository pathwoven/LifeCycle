package com.cc.service.impl;

import com.cc.constant.RedisConstants;
import com.cc.constant.UserActiveConstant;
import com.cc.dto.SeckillVoucherCacheDTO;
import com.cc.service.ICacheService;
import com.cc.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CacheService implements ICacheService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    IVoucherService voucherService;
    @Scheduled(cron = "0 0 3 * * ?")
    public void warmUpCache(){
        warmUpSeckill();
    }

    public void warmUpSeckill(){
        List<SeckillVoucherCacheDTO> cacheDTOList = voucherService.querySeckillVoucherForCacheToday();
        if(cacheDTOList == null || cacheDTOList.isEmpty()){return;}
        for(SeckillVoucherCacheDTO cacheDTO : cacheDTOList){
            String key = RedisConstants.SECKILL_STOCK_KEY + cacheDTO.getVoucherId();
            // stringRedisTemplate.opsForValue().set(key, String.valueOf(seckillVoucher.getStock()));
            stringRedisTemplate.opsForHash().put(key, "stock", cacheDTO.getStock());
            stringRedisTemplate.opsForHash().put(key, "begin", cacheDTO.getBeginKillTime());
            stringRedisTemplate.opsForHash().put(key, "end", cacheDTO.getEndKillTime());
        }

    }

    @Scheduled(cron = "0 30 3 * * ?")
    @Override
    public void recalculateAllUserScores() {
        // 限制缓存数量
        Long size = stringRedisTemplate.opsForZSet()
                .size(RedisConstants.USER_ACTIVE_KEY);
        if(size == null){return;}
        if(size > RedisConstants.USER_ACTIVE_MAX){
            // 删除多余的部分
            stringRedisTemplate.opsForZSet()
                    .removeRange(RedisConstants.USER_ACTIVE_KEY,
                            0, size - RedisConstants.USER_ACTIVE_MAX - 1);
        }
        // 衰减所有用户活跃度
        stringRedisTemplate.opsForZSet()
                .incrementScore(RedisConstants.USER_ACTIVE_KEY,
                        "*",
                        -UserActiveConstant.USER_ACTIVE_DECREASE);
        // 小于0的删除
        stringRedisTemplate.opsForZSet()
                .removeRangeByScore(RedisConstants.USER_ACTIVE_KEY,
                        Double.NEGATIVE_INFINITY,
                        - 0.1);

        // 移除无活跃度的用户feed
        removeInactiveUsers();
    }

    private void removeInactiveUsers(){
        // 移除无活跃度的用户的feed
        Set<String> keys = stringRedisTemplate.keys(RedisConstants.FEED_BOX_KEY + "*");
        for(String key : keys){
            String userIdStr = key.substring(RedisConstants.FEED_BOX_KEY.length());
            Long userId = Long.valueOf(userIdStr);
            Double score = stringRedisTemplate.opsForZSet().score(RedisConstants.USER_ACTIVE_KEY,userId);
            if(score == null){
                stringRedisTemplate.delete(key);
            }
        }
    }
}
