package com.smartdocs.aikb.module.chat.service;

import com.smartdocs.aikb.module.chat.dto.ChatMessageVO;
import com.smartdocs.aikb.module.chat.dto.ConversationCreateRequest;
import com.smartdocs.aikb.module.chat.dto.ConversationVO;
import com.smartdocs.aikb.module.chat.entity.ChatConversation;

import java.util.List;

/**
 * 会话 CRUD（不含 AI 流式）。
 */
public interface ConversationService {

    /** 列出当前用户的会话（按更新时间倒序）。 */
    List<ConversationVO> listMine(Long userId);

    /** 新建会话；title 为空时取「新会话」。 */
    ConversationVO create(Long userId, ConversationCreateRequest request);

    /** 重命名。 */
    ConversationVO rename(Long userId, Long conversationId, String title);

    /** 删除（逻辑删）。 */
    void delete(Long userId, Long conversationId);

    /** 取会话详情（含权限校验）。 */
    ConversationVO detail(Long userId, Long conversationId);

    /** 取消息列表（按时间正序）。 */
    List<ChatMessageVO> listMessages(Long userId, Long conversationId);

    /** 内部接口：取会话实体并校验属于该用户。供 RagChatService 调用，跳过 VO 转换。 */
    ChatConversation requireOwn(Long userId, Long conversationId);
}
