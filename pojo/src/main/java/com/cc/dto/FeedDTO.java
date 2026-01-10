package com.cc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedDTO {
    private Double cursor;
    private int type;    // 为0表示拉取所有类型，为1表示拉取关注的用户的动态
}
