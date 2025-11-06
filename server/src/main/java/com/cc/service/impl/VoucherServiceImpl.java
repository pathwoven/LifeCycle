package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.constant.RedisConstants;
import com.cc.dto.Result;
import com.cc.dto.VoucherAddSeckillDTO;
import com.cc.entity.SeckillVoucher;
import com.cc.entity.Voucher;
import com.cc.mapper.VoucherMapper;
import com.cc.service.ISeckillVoucherService;
import com.cc.service.IVoucherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public Long addSeckillVoucher(VoucherAddSeckillDTO voucherAddSeckillDTO) {
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(voucherAddSeckillDTO, voucher);
        // 保存优惠券
        save(voucher);
        Long id = voucher.getId();
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        BeanUtils.copyProperties(seckillVoucher, voucher);
        seckillVoucher.setVoucherId(id);
        seckillVoucherService.save(seckillVoucher);
        String key = RedisConstants.SECKILL_STOCK_KEY + seckillVoucher.getVoucherId();
        stringRedisTemplate.opsForValue().set(key, String.valueOf(seckillVoucher.getStock()));
        return id;
    }
}
