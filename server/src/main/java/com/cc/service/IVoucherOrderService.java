package com.cc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.entity.VoucherOrder;

public interface IVoucherOrderService extends IService<VoucherOrder> {
    Long seckillVoucher(Long userId, Long voucherId);

    void checkPayment(Long userId, Long voucherId, Long orderId);
    boolean cancelOrder(Long userId, Long voucherId, Long orderId);
}
