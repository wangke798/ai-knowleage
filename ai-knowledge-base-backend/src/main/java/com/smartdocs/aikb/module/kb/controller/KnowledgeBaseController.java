package com.smartdocs.aikb.module.kb.controller;

import com.smartdocs.aikb.common.result.PageVO;
import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.kb.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/kb")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @GetMapping
    public Result<PageVO<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(knowledgeBaseService.page(userId, page, size, keyword));
    }

    @GetMapping("/{kbId}")
    public Result<Map<String, Object>> detail(@PathVariable Long kbId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(knowledgeBaseService.detail(userId, kbId));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> params) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(knowledgeBaseService.create(userId, params));
    }

    @PutMapping("/{kbId}")
    public Result<Map<String, Object>> update(@PathVariable Long kbId, @RequestBody Map<String, Object> params) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(knowledgeBaseService.update(userId, kbId, params));
    }

    @DeleteMapping("/{kbId}")
    public Result<Void> delete(@PathVariable Long kbId) {
        Long userId = CurrentUserHolder.requireUserId();
        knowledgeBaseService.delete(userId, kbId);
        return Result.success();
    }
}
