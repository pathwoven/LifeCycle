package com.cc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.dto.Result;
import com.cc.dto.VoucherAddSeckillDTO;
import com.cc.entity.Voucher;

public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    Long addSeckillVoucher(VoucherAddSeckillDTO voucherAddSeckillDTO);
}
