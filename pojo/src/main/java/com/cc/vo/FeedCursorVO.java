package com.cc.vo;

import com.cc.dto.FeedBlogDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedCursorVO {
    private List<FeedBlogDTO> feedBlogDTOList;
    private Double cursor;

}
