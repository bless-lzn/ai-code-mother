package com.limou.aicodemother.service;

import com.limou.aicodemother.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.limou.aicodemother.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.limou.aicodemother.model.entity.ChatHistory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author 李振南
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存对话历史。
     *
     * @param appId       应用id
     * @param message     消息
     * @param messageType user/ai
     * @param userId      创建用户id
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用ID删除对话历史
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询条件
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 根据应用ID分页获取对话历史
     *
     * @param appId       应用ID
     * @param pageSize    页面大小
     * @param lastCreateTime 最后创建时间
     * @param loginUser   登录用户
     * @return 对话历史分页结果
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    int loadChatHistory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
