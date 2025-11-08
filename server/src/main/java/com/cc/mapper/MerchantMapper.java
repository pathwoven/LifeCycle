package com.cc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.entity.Merchant;
import org.apache.ibatis.annotations.Select;

public interface MerchantMapper extends BaseMapper<Merchant> {
    @Select("select * from tb_merchant where phone = #{phone}")
    Merchant findByPhone(String phone);
}
