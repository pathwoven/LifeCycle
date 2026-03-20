package com.cc.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("blog_ai_meta")
public class BlogAiMeta implements Serializable {
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
