package com.cc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoucherAddSeckillDTO {
    private Long shopId;
    private String title;
    private String subTitle;
    private String rules;
    private Long payValue;
    private Long actualValue;
    private Integer type;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer stock;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private LocalDateTime beginKillTime;
    private LocalDateTime endKillTime;
}
