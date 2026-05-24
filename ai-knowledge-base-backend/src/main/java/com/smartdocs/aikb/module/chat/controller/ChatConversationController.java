package com.smartdocs.aikb.module.chat.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
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

    /**
     * 获取当前用户会话列表。
     *
     * @param keyword     标题关键词（可选）
     * @param favoriteOnly 仅显示收藏（可选）
     */
    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean favoriteOnly) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(conversationService.listMine(userId, keyword, favoriteOnly));
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

    /** 切换收藏状态。 */
    @PostMapping("/{conversationId}/favorite")
    public Result<Map<String, Object>> toggleFavorite(@PathVariable Long conversationId) {
        Long userId = CurrentUserHolder.requireUserId();
        int newVal = conversationService.toggleFavorite(userId, conversationId);
        return Result.success(Map.of("isFavorite", newVal == 1));
    }

    /**
     * 导出会话内容。
     *
     * @param format 格式：markdown（默认）/ text / json
     */
    @GetMapping("/{conversationId}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long conversationId,
                                         @RequestParam(defaultValue = "markdown") String format) {
        Long userId = CurrentUserHolder.requireUserId();
        String content = conversationService.exportConversation(userId, conversationId, format);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        String ext = "json".equalsIgnoreCase(format) ? "json" : "text".equalsIgnoreCase(format) ? "txt" : "md";
        String filename = "conversation_" + conversationId + "." + ext;
        String mediaType = "json".equalsIgnoreCase(format) ? "application/json" : "text/plain";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(mediaType + ";charset=UTF-8"))
                .body(bytes);
    }
}
