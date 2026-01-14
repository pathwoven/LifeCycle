package com.cc.utils;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.cc.config.AlipayConfig;
import com.cc.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Slf4j
public class Alipay {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AlipayConfig alipayConfig;

    public String createWapPay(OrderDTO orderDTO) {
        try {
            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            request.setReturnUrl(alipayConfig.getReturnUrl());

            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();

            model.setOutTradeNo(orderDTO.getOutTradeNo().toString());
            BigDecimal amount = BigDecimal.valueOf(orderDTO.getTotalAmount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            model.setTotalAmount(amount.toString());
            model.setSubject(orderDTO.getSubject());
            model.setProductCode("QUICK_WAP_WAY");
            model.setQuitUrl(orderDTO.getQuitUrl());
            model.setTimeExpire(orderDTO.getExpireTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            request.setBizModel(model);

            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "POST");

            if(!response.isSuccess()){
                throw new RuntimeException("支付宝wap支付调用api失败");
            }

            return response.getBody(); // 返回给客户端的订单信息
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String createAppPay(OrderDTO orderDTO){
        try {
            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            request.setReturnUrl(alipayConfig.getReturnUrl());

            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setOutTradeNo(orderDTO.getOutTradeNo().toString());
            BigDecimal amount = BigDecimal.valueOf(orderDTO.getTotalAmount())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            model.setTotalAmount(amount.toString());
            model.setSubject(orderDTO.getSubject());
            model.setProductCode("QUICK_MSECURITY_PAY");
            request.setBizModel(model);

            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            return response.getBody(); // 返回给客户端的订单信息
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询订单状态
     * @param outTradeNo
     * @return 1-等待支付，2-支付成功，3-交易结束（也是成功的一种状态），4-交易关闭，5-其他状态，null-查询失败
     */
    public Integer queryTradeStatus(Long outTradeNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();

            model.setOutTradeNo(outTradeNo.toString());
            request.setBizModel(model);

            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                switch (tradeStatus) {
                    case "TRADE_FINISHED":
                        return 3;
                    case "TRADE_SUCCESS":
                        return 2;
                    case "WAIT_BUYER_PAY":
                        return 1;
                    case "TRADE_CLOSED":
                        return 4;
                    default:
                        return 5;
                }
            } else {
                log.error("支付宝查询订单失败，outTradeNo={}, msg={}", outTradeNo, response.getSubMsg());
                return null;
            }
        } catch (AlipayApiException e) {
            log.error("支付宝查询订单异常，outTradeNo={}", outTradeNo, e);
            return null;
        }
    }

    /**
     * 进行验签，同时会校验seller_id和app_id
     * @param params
     * @return
     */
    public boolean rsaVerify(Map<String, String> params) {
        try {
            boolean success = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );
            if (!success) return false;
            // 校验app_id和seller_id
            String appId = params.get("app_id");
            String sellerId = params.get("seller_id");
            if (!alipayConfig.getAppId().equals(appId)) {
                log.error("支付宝验签失败，app_id不匹配，传入app_id={}，正确app_id={}", appId, alipayConfig.getAppId());
                return false;
            }
            if (!alipayConfig.getSellerId().equals(sellerId)) {
                log.error("支付宝验签失败，seller_id不匹配，传入seller_id={}，正确seller_id={}", sellerId, alipayConfig.getSellerId());
                return false;
            }
            return true;
        } catch (AlipayApiException e) {
            log.error("支付宝验签失败", e);
            return false;
        }
    }
}
