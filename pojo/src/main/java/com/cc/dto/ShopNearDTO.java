package com.cc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopNearDTO {
    private Integer typeId;
    private Double x;
    private Double y;
    private Double distance;
    private int page;
}
