package com.limou.aicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建AiCodeGeneratorService的工厂类
 * 调用AI并且创建AI的返回值转换为string
 */
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    //    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
//    }
//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
//    }
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }


}
