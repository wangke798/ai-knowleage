package com.smartdocs.aikb.module.chat.service;

import com.smartdocs.aikb.module.chat.dto.ChatStreamRequest;
import com.smartdocs.aikb.module.chat.dto.CitationVO;

import java.util.List;

/**
 * RAG 流式问答。把检索 + LLM 调用 + 持久化封装在一起，
 * 通过 listener 把 token / 引用 / 结束事件回调出去（让 Controller 决定怎么落到 SSE）。
 */
public interface RagChatService {

    /**
     * 发起一次流式问答。同步阻塞直到流结束（或异常），其间通过 listener 推 token。
     *
     * @param userId   当前用户
     * @param request  对话请求（含 question / topK / conversationId 等）
     * @param listener token / 引用 / 结束 / 异常 回调
     */
    void streamChat(Long userId, ChatStreamRequest request, StreamListener listener);

    interface StreamListener {
        /** 会话已就绪（含新建场景），先把 conversationId 告诉前端 */
        void onConversationReady(Long conversationId);

        /** 用户消息已落库（含 messageId，给前端固定占位用） */
        void onUserMessageSaved(Long messageId);

        /** 检索结果，告诉前端「我看了哪些引用」 */
        void onCitations(List<CitationVO> citations);

        /** 一片 token */
        void onToken(String token);

        /** 助手消息已落库 */
        void onAssistantMessageSaved(Long messageId, String fullContent);

        /** 流结束（正常） */
        void onComplete();

        /** 出错 */
        void onError(Throwable err);
    }
}
