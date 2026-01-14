package com.cc.entity;

import lombok.Data;

@Data
public class Refund {
    private Long id;
    private Long paymentId;
    private Long orderId;
    private Long refundAmount;
    private String status;
    private String reason;
    private String createTime;
    private String updateTime;
}
