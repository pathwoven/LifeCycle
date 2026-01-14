package com.cc.controller;


import com.alipay.api.internal.util.AlipaySignature;
import com.cc.constant.RedisConstants;
import com.cc.dto.Result;
import com.cc.entity.VoucherOrder;
import com.cc.service.IVoucherOrderService;
import com.cc.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/voucher-order")
@Slf4j
public class VoucherOrderController {
    @Autowired
    private IVoucherOrderService voucherOrderService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @PostMapping("/seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        Long id = voucherOrderService.seckillVoucher(UserHolder.getUserId(), voucherId);
        if(id == null) {return Result.fail("下单失败");}
        return Result.ok(id);
    }

    /**
     * 长轮询获取订单的生成状态
     * redis中， 0 正在生成订单,  -1 库存不足 / 秒杀失败
     * @param orderId
     * @return  正在生成时，返回ok(0);生成成功时返回订单信息;失败时返回失败信息
     */
    @GetMapping("/status/{id}")
    public Result getVoucherOrderStatus(@PathVariable("id") Long orderId) {
        long start = System.currentTimeMillis();
        while (true) {
            Integer status = voucherOrderService.getStatus(orderId);
            if (status == null) {
                return Result.fail("订单异常");
            }
            // 秒杀失败
            if (status == -1) {
                return Result.fail("库存不足，秒杀失败");
            }
            // 订单信息已生成
            if (status != 0) {
                VoucherOrder voucherOrder = voucherOrderService.getById(orderId);
                if(voucherOrder == null){
                    log.error("订单状态为已生成，但查询订单不存在，订单id：{}", orderId);
                    return Result.fail("订单不存在");
                }
                return Result.ok(voucherOrder);
            }
            // 订单正在生成，继续等待
            // 超时返回
            if (System.currentTimeMillis() - start > 15000) { // 超时时间 15 秒
                return Result.ok(0);
            }
            try {
                Thread.sleep(200); // 每 200ms 检查一次
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Result.fail("线程被中断");
            }
        }
    }

}
