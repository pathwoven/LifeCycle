package com.cc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.entity.VoucherOrder;
import org.apache.ibatis.annotations.Select;

public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {
    @Select("select status from life_cycle.tb_voucher_order where id = #{orderId}")
    Integer queryStatus(Long orderId);
}
