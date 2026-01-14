package com.cc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long outTradeNo;
    private Long totalAmount;
    private String subject;
    private String quitUrl;
    private LocalDateTime expireTime;
}
