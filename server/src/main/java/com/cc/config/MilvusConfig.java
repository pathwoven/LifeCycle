package com.cc.config;

import com.cc.properties.MilvusProperties;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {
    @Autowired
    private MilvusProperties milvusProperties;
    @Bean
    public MilvusClientV2 milvusClient(){
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri("http://localhost:19530")
                .token("root:milvus")
                .build();
    }
}
