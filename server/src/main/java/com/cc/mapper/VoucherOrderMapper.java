package com.cc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.entity.VoucherOrder;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {
    @Select("select status from life_cycle.order where id = #{orderId}")
    Integer queryStatus(Long orderId);

    @Update("update life_cycle.order set status = 2, update_time = now(), pay_type = #{channel} where id = #{orderId} and status = 1")
    Integer updateStatusSuccess(Long orderId, int channel);
}
