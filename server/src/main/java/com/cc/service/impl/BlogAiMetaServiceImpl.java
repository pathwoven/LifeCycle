package com.cc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.entity.BlogAiMeta;
import com.cc.mapper.BlogAiMetaMapper;
import com.cc.service.IBlogAiMetaService;
import org.springframework.stereotype.Service;

@Service
public class BlogAiMetaServiceImpl extends ServiceImpl<BlogAiMetaMapper, BlogAiMeta> implements IBlogAiMetaService {
}
