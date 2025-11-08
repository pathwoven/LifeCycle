package com.cc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.dto.SeckillVoucherCacheDTO;
import com.cc.entity.SeckillVoucher;

import java.util.List;

public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {
    List<SeckillVoucherCacheDTO> querySeckillVoucherForCacheToday(List<Long> voucherIdList);
}
