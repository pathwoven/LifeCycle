package com.cc.ai.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 博客向量服务类，负责处理博客内容的向量化和相关操作
 */
@Service
public class BlogVectorService {
    @Autowired
    private MilvusClientV2 milvusClient;

    public void insert(Long id, float[] vector) {
        // 构建插入请求
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        Gson gson = new Gson();
        jsonObject.add("vector", gson.toJsonTree(vector));
        InsertReq insertReq = InsertReq.builder()
                .collectionName("blog")
                .data(Collections.singletonList(jsonObject))
                .build();
        milvusClient.insert(insertReq);
    }
}
