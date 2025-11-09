package com.cc.service.impl;

import com.cc.constant.RedisConstants;
import com.cc.dto.SeckillVoucherCacheDTO;
import com.cc.service.ICacheService;
import com.cc.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
