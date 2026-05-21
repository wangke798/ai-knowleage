package com.smartdocs.aikb.module.chat.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.chat.dto.ChatMessageVO;
import com.smartdocs.aikb.module.chat.dto.ConversationCreateRequest;
import com.smartdocs.aikb.module.chat.dto.ConversationRenameRequest;
import com.smartdocs.aikb.module.chat.dto.ConversationVO;
import com.smartdocs.aikb.module.chat.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话 CRUD（不含流式回答）。
 */
@Validated
@RestController
@RequestMapping("/chat/conversations")
@RequiredArgsConstructor
public class ChatConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public Result<List<ConversationVO>> list() {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.listMine(userId));
    }

    @PostMapping
    public Result<ConversationVO> create(@Valid @RequestBody ConversationCreateRequest body) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.create(userId, body));
    }

    @GetMapping("/{conversationId}")
    public Result<ConversationVO> detail(@PathVariable Long conversationId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.detail(userId, conversationId));
    }

    @PutMapping("/{conversationId}")
    public Result<ConversationVO> rename(@PathVariable Long conversationId,
                                         @Valid @RequestBody ConversationRenameRequest body) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.rename(userId, conversationId, body.getTitle()));
    }

    @DeleteMapping("/{conversationId}")
    public Result<Void> delete(@PathVariable Long conversationId) {
        Long userId = CurrentUserHolder.requireUserId();
        conversationService.delete(userId, conversationId);
        return Result.success();
    }

    @GetMapping("/{conversationId}/messages")
    public Result<List<ChatMessageVO>> messages(@PathVariable Long conversationId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.listMessages(userId, conversationId));
    }
}
