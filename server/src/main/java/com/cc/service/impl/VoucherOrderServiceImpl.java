package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.entity.SeckillVoucher;
import com.cc.entity.VoucherOrder;
import com.cc.mapper.VoucherOrderMapper;
import com.cc.service.ISeckillVoucherService;
import com.cc.service.IVoucherOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    ISeckillVoucherService seckillVoucherService;
    @Override
    public Long seckillVoucher(Long userId, Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if(seckillVoucher == null) {return null;}
        // 判断是否在抢购期
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(seckillVoucher.getBeginTime())) {return null;}
        if(now.isAfter(seckillVoucher.getEndTime())) {return null;}
        // 判断库存
        return null;
    }
}
