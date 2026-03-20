package com.cc.ai.service;

import com.cc.ai.agent.BlogAiAgent;
import com.cc.ai.agent.EmbeddingAgent;
import com.cc.ai.agent.SummaryAgent;
import com.cc.entity.Blog;
import com.cc.entity.BlogAiMeta;
import com.cc.service.IBlogAiMetaService;
import com.cc.service.IBlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlogAiService {
    @Autowired
    private BlogAiAgent blogAiAgent;
    @Autowired
    private IBlogService blogService;
    @Autowired
    private EmbeddingAgent embeddingAgent;
    @Autowired
    private IBlogAiMetaService blogAiMetaService;
    @Autowired
    private BlogVectorService blogVectorService;

    /** 处理blog生成
     *
     */
    public void processBlogGeneration(Long blogId) {
        // 获取博客内容
        String blogContent = blogService.lambdaQuery()
                .eq(Blog::getId, blogId)
                .select(Blog::getContent)
                .one()
                .getContent();
        // 总结
        String summary = blogAiAgent.summary(blogContent);
        // 生成标签
        List<String> tags = blogAiAgent.generateTags(blogContent);
        // 得到嵌入
        float[] embedding = embeddingAgent.getEmbedding(blogContent);
        // 存入数据库 todo 异常处理
        BlogAiMeta blogAiMeta = new BlogAiMeta();
        blogAiMeta.setBlogId(blogId);
        blogAiMeta.setSummary(summary);
        blogAiMeta.setTags(String.join(",", tags));
        LocalDateTime now = LocalDateTime.now();
        blogAiMeta.setCreateTime(now);
        blogAiMeta.setUpdateTime(now);
        blogAiMetaService.save(blogAiMeta);
        // 存入向量数据库
        blogVectorService.insert(blogId, embedding);
    }
}
