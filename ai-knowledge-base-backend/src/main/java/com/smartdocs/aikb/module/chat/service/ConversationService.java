package com.smartdocs.aikb.module.chat.service;

import com.smartdocs.aikb.module.chat.entity.ChatConversation;

import java.util.List;
import java.util.Map;

public interface ConversationService {

    /** 查询当前用户的会话列表，支持关键词搜索和收藏过滤。 */
    List<Map<String, Object>> listMine(Long userId, String keyword, Boolean favoriteOnly);

    Map<String, Object> create(Long userId, Map<String, Object> params);

    Map<String, Object> rename(Long userId, Long conversationId, String title);

    void delete(Long userId, Long conversationId);

    Map<String, Object> detail(Long userId, Long conversationId);

    List<Map<String, Object>> listMessages(Long userId, Long conversationId);

    /** 切换收藏状态，返回最新 isFavorite 值（0/1）。 */
    int toggleFavorite(Long userId, Long conversationId);

    /** 导出会话为 Markdown / JSON / text 格式，返回文本内容。 */
    String exportConversation(Long userId, Long conversationId, String format);

    /** 内部接口：取会话实体并校验属于该用户，供 RagChatService 调用。 */
    ChatConversation requireOwn(Long userId, Long conversationId);
}
