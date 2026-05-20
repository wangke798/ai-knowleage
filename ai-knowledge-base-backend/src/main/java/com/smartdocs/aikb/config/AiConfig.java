package com.smartdocs.aikb.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Dev 环境 AI 配置：指定 Ollama EmbeddingModel 为主 Bean，
 * 解决 PgVectorStoreAutoConfiguration 因同时存在 Ollama 和 OpenAI
 * 两个 EmbeddingModel 而无法注入的问题。
 */
@Configuration
@Profile("dev")
public class AiConfig {

    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel(
            @Qualifier("ollamaEmbeddingModel") EmbeddingModel ollamaEmbeddingModel) {
        return ollamaEmbeddingModel;
    }
}
