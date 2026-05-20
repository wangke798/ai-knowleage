package com.smartdocs.aikb.module.kb.controller;

import com.smartdocs.aikb.common.result.PageVO;
import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.kb.dto.KbUpsertRequest;
import com.smartdocs.aikb.module.kb.dto.KbVO;
import com.smartdocs.aikb.module.kb.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "知识库管理")
@RestController
@RequestMapping("/kb")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @Operation(summary = "分页查询当前用户可见的知识库")
    @GetMapping
    public Result<PageVO<KbVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @Parameter(description = "名称模糊匹配") @RequestParam(required = false) String keyword) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(knowledgeBaseService.page(userId, page, size, keyword));
    }

    @Operation(summary = "获取知识库详情")
    @GetMapping("/{kbId}")
    public Result<KbVO> detail(@PathVariable Long kbId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(knowledgeBaseService.detail(userId, kbId));
    }

    @Operation(summary = "创建知识库")
    @PostMapping
    public Result<KbVO> create(@Valid @RequestBody KbUpsertRequest request) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(knowledgeBaseService.create(userId, request));
    }

    @Operation(summary = "更新知识库")
    @PutMapping("/{kbId}")
    public Result<KbVO> update(@PathVariable Long kbId, @Valid @RequestBody KbUpsertRequest request) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(knowledgeBaseService.update(userId, kbId, request));
    }

    @Operation(summary = "删除知识库（仅所有者）")
    @DeleteMapping("/{kbId}")
    public Result<Void> delete(@PathVariable Long kbId) {
        Long userId = CurrentUserHolder.requireUserId();
        knowledgeBaseService.delete(userId, kbId);
        return Result.success();
    }
}
