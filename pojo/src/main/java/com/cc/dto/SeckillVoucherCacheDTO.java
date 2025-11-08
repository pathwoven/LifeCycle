package com.cc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillVoucherCacheDTO {
    private Long voucherId;
    private Integer stock;
    private LocalDateTime beginKillTime;
    private LocalDateTime endKillTime;
}
