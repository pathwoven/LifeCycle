package com.cc.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

/**
 * 为博客相关的AI处理提供服务的类
 */
@AiService
public interface BlogAiAgent {
    @SystemMessage("你是一个博客处理助手，负责处理用户的博客内容，提取关键信息，并生成相关的总结和推荐。" +
            "请根据用户提供的博客内容，提取出关键信息和主要观点，并生成一个简洁的总结。")
    String summary(String blogContent);

    @SystemMessage("你是一个标签生成助手，负责根据用户的博客内容生成相关的标签。" +
            "请根据用户提供的博客内容，提取出关键信息和主要观点，并生成相关的标签。")
    List<String> generateTags(String blogContent);
}
