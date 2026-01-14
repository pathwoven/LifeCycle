package com.cc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.entity.VoucherOrder;

import java.util.Map;

public interface IVoucherOrderService extends IService<VoucherOrder> {
    Long seckillVoucher(Long userId, Long voucherId);

    void checkPayment(Long userId, Long voucherId, Long orderId);
    boolean cancelOrder(Long userId, Long voucherId, Long orderId);

    String getPayLink(Long orderId, Integer type);

    Integer getStatus(Long orderId);

    boolean alipayPayCallback(Map<String, String> params);
}
