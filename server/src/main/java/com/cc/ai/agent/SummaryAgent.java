package com.cc.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.stereotype.Component;

@AiService
public interface SummaryAgent {
    @SystemMessage("你是一个总结助手，负责总结用户的博客内容，其中博客大多与美食娱乐相关。" +
            "请根据用户提供的博客内容，提取出关键信息和主要观点，并生成一个简洁的总结。")
    String chat(String blogContent);
}
