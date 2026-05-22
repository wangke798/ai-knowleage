package com.smartdocs.aikb.module.chat.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会话 CRUD（不含流式回答）。
 */
@RestController
@RequestMapping("/chat/conversations")
@RequiredArgsConstructor
public class ChatConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.listMine(userId));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> params) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.create(userId, params));
    }

    @GetMapping("/{conversationId}")
    public Result<Map<String, Object>> detail(@PathVariable Long conversationId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.detail(userId, conversationId));
    }

    @PutMapping("/{conversationId}")
    public Result<Map<String, Object>> rename(@PathVariable Long conversationId,
                                              @RequestBody Map<String, Object> params) {
        Long userId = CurrentUserHolder.requireUserId();
        String title = (String) params.get("title");
        return Result.success(conversationService.rename(userId, conversationId, title));
    }

    @DeleteMapping("/{conversationId}")
    public Result<Void> delete(@PathVariable Long conversationId) {
        Long userId = CurrentUserHolder.requireUserId();
        conversationService.delete(userId, conversationId);
        return Result.success();
    }

    @GetMapping("/{conversationId}/messages")
    public Result<List<Map<String, Object>>> messages(@PathVariable Long conversationId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.listMessages(userId, conversationId));
    }
}
