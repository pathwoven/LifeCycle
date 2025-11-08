package com.cc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillOrderMessage {
    Long seckillOrderId;
    Long userId;
    Long voucherId;
}
