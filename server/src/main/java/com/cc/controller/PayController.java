package com.cc.controller;

import com.cc.dto.Result;
import com.cc.service.IVoucherOrderService;
import com.cc.utils.Alipay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController("/pay")
@Slf4j
public class PayController {
    @Autowired
    private IVoucherOrderService voucherOrderService;
    @Autowired
    private Alipay alipay;

    /**
     * 长轮询订单的支付状态，在刚支付时才会调用这个接口
     * @param orderId
     * @return success:0-未支付，1-支付成功，2-支付关闭。fail表示订单异常
     */
    @GetMapping("/status/{orderId}/{outTradeNo}")
    public Result getPayStatus(@PathVariable("orderId") Long orderId, @PathVariable("outTradeNo") Long outTradeNo) {
        long start = System.currentTimeMillis();
        while (true) {
            Integer status = voucherOrderService.getStatus(orderId);
            if (status == null) {
                return Result.fail("订单异常");
            }
            // 支付完成
            if (status == 2) {
                return  Result.ok(1);
            }else if(status == -1){
                log.error("秒杀失败，订单已取消，该接口不该被调用，orderId={}", orderId);
                return Result.fail("秒杀失败，订单已取消");
            }else if(status != 1){
                return Result.ok(1);
            }
            // 超时10秒
            if(start - System.currentTimeMillis() > 10000) {
                // 尝试去支付宝拉取最新状态
                status = alipay.queryTradeStatus(outTradeNo);
                if(status == null) {
                    return Result.fail("订单异常");
                }
                if(status == 2 || status == 3) {
                    return Result.ok(1);
                }
                if(status == 1) return Result.ok(0);
                if(status == 4) return Result.ok(2);
                return Result.fail("未知状态");
            }
            // 继续等待
            try {
                Thread.sleep(200); // 每 200ms 检查一次
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Result.fail("线程被中断");
            }
        }
    }

    /**
     * 获取支付链接
     * @param orderId
     * @param type
     * @return
     */
    @GetMapping("/{id}/{type}")
    public Result getPayLink(@PathVariable("id") Long orderId, @PathVariable("type") Integer type) {
        // 校验订单状态
        Integer status = voucherOrderService.getStatus(orderId);
        if(status == null) {
            log.error("获取支付链接失败，订单不存在，orderId={}", orderId);
            return Result.fail("订单不存在");
        }
        if(status != 1) {
            return Result.fail("重复或已取消的订单，无法获取链接");
        }

        String payLink = voucherOrderService.getPayLink(orderId, type);
        if(payLink == null) {
            return Result.fail("获取支付链接失败");
        }
        return Result.ok(payLink);
    }



    /**
     * 返回给支付宝的回调接口
     * 返回值为字符串，表示是否接收成功
     * @param request
     * @return
     */
    @PostMapping("/alipay/callback")
    public String alipayCallback(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = String.join(",", values);
            params.put(name, valueStr);
        }

        return voucherOrderService.alipayPayCallback(params) ? "success" : "failure";
    }

}
