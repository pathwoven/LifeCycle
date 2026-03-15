package com.cc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "milvus")
@Data
public class MilvusProperties {
    private String uri;
    private String database;
    private String username;
    private String password;
}
