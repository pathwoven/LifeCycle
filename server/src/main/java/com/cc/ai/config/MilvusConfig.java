package com.cc.ai.config;

import com.cc.properties.MilvusProperties;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {
    @Autowired
    private MilvusProperties milvusProperties;

    private static final String blogCollection = "blog_collection";
    private static final String userCollection = "user_collection";
    @Bean
    public MilvusClientV2 milvusClient(){
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(milvusProperties.getUri())
                .username(milvusProperties.getUsername())
                .password(milvusProperties.getPassword())
                .build();
        MilvusClientV2 milvusClient = new MilvusClientV2(connectConfig);

        // 加载user和blog集合到内存中
        LoadCollectionReq loadBlogCollectionReq = LoadCollectionReq.builder()
                .collectionName(blogCollection)
                .build();
        LoadCollectionReq loadUserCollectionReq = LoadCollectionReq.builder()
                .collectionName(userCollection)
                .build();

        milvusClient.loadCollection(loadBlogCollectionReq);
        milvusClient.loadCollection(loadUserCollectionReq);

        return milvusClient;
    }
}
