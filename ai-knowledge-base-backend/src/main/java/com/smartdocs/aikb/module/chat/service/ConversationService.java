package com.smartdocs.aikb.module.chat.service;

import com.smartdocs.aikb.module.chat.entity.ChatConversation;

import java.util.List;
import java.util.Map;

public interface ConversationService {

    List<Map<String, Object>> listMine(Long userId);

    Map<String, Object> create(Long userId, Map<String, Object> params);

    Map<String, Object> rename(Long userId, Long conversationId, String title);

    void delete(Long userId, Long conversationId);

    Map<String, Object> detail(Long userId, Long conversationId);

    List<Map<String, Object>> listMessages(Long userId, Long conversationId);

    /** 内部接口：取会话实体并校验属于该用户，供 RagChatService 调用。 */
    ChatConversation requireOwn(Long userId, Long conversationId);
}
