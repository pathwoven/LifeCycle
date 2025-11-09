package com.cc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogPublishDTO {
    private Long shopId;
    private String title;
    private String images;
    private String content;
}
