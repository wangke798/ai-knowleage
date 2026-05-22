package com.smartdocs.aikb.module.chat.service;

import java.util.List;
import java.util.Map;

/**
 * RAG 流式问答。通过 listener 回调 token / 引用 / 结束事件。
 * params 字段：question / conversationId / kbId / topK / similarityThreshold
 */
public interface RagChatService {

    void streamChat(Long userId, Map<String, Object> params, StreamListener listener);

    interface StreamListener {
        void onConversationReady(Long conversationId);
        void onUserMessageSaved(Long messageId);
        void onCitations(List<Map<String, Object>> citations);
        void onToken(String token);
        void onAssistantMessageSaved(Long messageId, String fullContent);
        void onComplete();
        void onError(Throwable err);
    }
}
