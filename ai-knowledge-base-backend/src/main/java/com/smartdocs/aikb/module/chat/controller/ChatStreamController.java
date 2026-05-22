package com.smartdocs.aikb.module.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.chat.service.RagChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * RAG 流式对话 SSE 入口。
 *
 * <p>事件格式（{@code event} 字段）：
 * <pre>
 *   conversation : { "id": 123 }
 *   user_message : { "id": 456 }
 *   citations    : [ {chunkId, docId, docName, seq, snippet, score}, ... ]
 *   token        : { "v": "片段内容" }
 *   assistant_message : { "id": 789, "content": "完整答案" }
 *   done         : {}
 *   error        : { "message": "..." }
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatStreamController {

    /** SSE 整体超时（含 LLM 调用），单位毫秒。设大一点避免长答案被切。 */
    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    private final RagChatService ragChatService;
    private final ObjectMapper objectMapper;
    private final AsyncTaskExecutor applicationTaskExecutor;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody Map<String, Object> params) {
        Long userId = CurrentUserHolder.requireUserId();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onTimeout(() -> {
            log.warn("[Chat SSE] timeout userId={}", userId);
            sendErrorQuietly(emitter, "对话超时");
            emitter.complete();
        });
        emitter.onError(t -> log.warn("[Chat SSE] connection error: {}", t.toString()));

        applicationTaskExecutor.execute(() -> ragChatService.streamChat(userId, params, new RagChatService.StreamListener() {
            @Override public void onConversationReady(Long conversationId) {
                send(emitter, "conversation", Map.of("id", conversationId));
            }
            @Override public void onUserMessageSaved(Long messageId) {
                send(emitter, "user_message", Map.of("id", messageId));
            }
            @Override public void onCitations(List<Map<String, Object>> citations) {
                send(emitter, "citations", citations);
            }
            @Override public void onToken(String token) {
                send(emitter, "token", Map.of("v", token));
            }
            @Override public void onAssistantMessageSaved(Long messageId, String fullContent) {
                send(emitter, "assistant_message", Map.of("id", messageId, "content", fullContent));
            }
            @Override public void onComplete() {
                send(emitter, "done", Map.of());
                emitter.complete();
            }
            @Override public void onError(Throwable err) {
                sendErrorQuietly(emitter, err.getMessage() == null ? "对话失败" : err.getMessage());
                emitter.complete();
            }
        }));

        return emitter;
    }

    private void send(SseEmitter emitter, String event, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(event).data(toJson(payload), MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException ex) {
            // 连接已断开 / 已 complete 时 send 会失败；不抛出，避免下游业务异常
            log.debug("[Chat SSE] send {} dropped: {}", event, ex.toString());
        }
    }

    private void sendErrorQuietly(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error")
                    .data(toJson(Map.of("message", message)), MediaType.APPLICATION_JSON));
        } catch (Exception ignored) {
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{\"_err\":\"serialize\"}";
        }
    }
}
