package com.cc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogAiMeta {
    private Long blogId;
    private String summary;
    private String tags;   // todo
    private String categories;
    private byte riskLevel;
    private double recommendScore;
    private byte status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
