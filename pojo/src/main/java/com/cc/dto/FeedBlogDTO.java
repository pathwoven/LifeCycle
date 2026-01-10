package com.cc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedBlogDTO {
    private Long blogId;
    private Long shopId;
    private String title;
    private String images;
    private String content;
    private Long userId;
    private String userName;
    private String userIcon;
    private Integer liked;
    private Integer comments;
}
