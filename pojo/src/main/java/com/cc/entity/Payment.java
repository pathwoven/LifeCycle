package com.cc.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    private Long id;

    private Long orderId;
    private Integer channel;
    private Long outTradeNo;
    private String payUrl;

    private Long amount;
    private Integer status;

    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
