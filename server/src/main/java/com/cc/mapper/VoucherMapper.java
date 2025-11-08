package com.cc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.dto.SeckillVoucherCacheDTO;
import com.cc.entity.Voucher;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);

    @Select("select id from tb_voucher where type = 1 and status = 1")
    List<Long> querySeckillVoucherForCacheToday();
}
