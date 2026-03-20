package com.cc.ai.agent;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingAgent {
    @Autowired
    private EmbeddingModel embeddingModel;

    public float[] getEmbedding(String text) {
        return embeddingModel.embed(text).content().vector();
    }
}
