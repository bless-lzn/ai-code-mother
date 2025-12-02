package com.limou.aicodemother.ai;

import cn.hutool.log.Log;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.limou.aicodemother.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 创建AiCodeGeneratorService的工厂类
 * 调用AI并且创建AI的返回值转换为string
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;


    private final Cache<Long,AiCodeGeneratorService> serviceCache= Caffeine.newBuilder()
            .maximumSize(1000)//设置缓存的最大容量
            .expireAfterWrite(30 * 60, TimeUnit.MILLISECONDS)
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                // 缓存项被移除时执行+ cause);
                log.debug("AI 服务实例化被移除，appId：{},原因:{}", key, cause);
            })
            .build();

    //    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
//    }
//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
//    }
//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService(long appId) {
//
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryStore(redisChatMemoryStore)
//                .id(appId)
//                .maxMessages(20)
//                .build();
//
//        return AiServices.builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(streamingChatModel)
//                .chatMemory(chatMemory)
//                .build();
//    }

    @Bean
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(appId)
                .maxMessages(20)
                .build();
        int loadCount = chatHistoryService.loadChatHistory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }


    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService(Long appId) {
        //从缓存当中拿去缓存
        return serviceCache.get(appId, this::getAiCodeGeneratorService);
    }



}
