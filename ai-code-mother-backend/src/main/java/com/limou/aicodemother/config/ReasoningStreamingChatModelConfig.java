package com.limou.aicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "langchain4j.open-ai.reason-stream-chat-model")
@Configuration
@Data
public class ReasoningStreamingChatModelConfig {

    private String apiKey;

    private String modelName;

    private String baseUrl;

    private Integer maxTokens;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }


}
