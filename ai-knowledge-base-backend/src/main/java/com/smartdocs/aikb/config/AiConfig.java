package com.smartdocs.aikb.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Dev 环境 AI 配置：声明主 EmbeddingModel / ChatModel / ChatClient。
 *
 * <p>当前默认使用通义百炼（通过 OpenAI 兼容协议接入，spring-ai 暴露 Bean 名
 * {@code openAiEmbeddingModel} / {@code openAiChatModel}）。Ollama 作为备份保留，
 * 切换时改 @Qualifier 名即可。
 */
@Configuration
@Profile("dev")
public class AiConfig {

    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel(
            @Qualifier("openAiEmbeddingModel") EmbeddingModel openAiEmbeddingModel) {
        return openAiEmbeddingModel;
    }

    @Bean
    @Primary
    public ChatModel primaryChatModel(
            @Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        return openAiChatModel;
    }

    @Bean
    public ChatClient chatClient(ChatModel primaryChatModel) {
        return ChatClient.builder(primaryChatModel).build();
    }
}
