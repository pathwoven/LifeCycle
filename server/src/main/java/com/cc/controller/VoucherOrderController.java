package com.cc.controller;


import com.cc.dto.Result;
import com.cc.service.IVoucherOrderService;
import com.cc.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Autowired
    IVoucherOrderService voucherOrderService;
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        Long id = voucherOrderService.seckillVoucher(UserHolder.getUserId(), voucherId);
        if(id == null) {return Result.fail("下单失败");}
        return Result.ok(id);
    }
}
