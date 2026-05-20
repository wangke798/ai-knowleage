package com.smartdocs.aikb.module.kb.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.kb.dto.KbMemberRequest;
import com.smartdocs.aikb.module.kb.dto.KbMemberVO;
import com.smartdocs.aikb.module.kb.service.KbMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "知识库成员管理")
@RestController
@RequestMapping("/kb/{kbId}/members")
@RequiredArgsConstructor
@Validated
public class KbMemberController {

    private final KbMemberService kbMemberService;

    @Operation(summary = "查看成员列表")
    @GetMapping
    public Result<List<KbMemberVO>> list(@PathVariable Long kbId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(kbMemberService.list(userId, kbId));
    }

    @Operation(summary = "添加成员")
    @PostMapping
    public Result<KbMemberVO> add(@PathVariable Long kbId, @Valid @RequestBody KbMemberRequest request) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(kbMemberService.add(userId, kbId, request));
    }

    @Operation(summary = "更新成员角色")
    @PutMapping("/{memberId}")
    public Result<KbMemberVO> updateRole(@PathVariable Long kbId,
                                         @PathVariable Long memberId,
                                         @Valid @RequestBody RoleUpdateRequest request) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(kbMemberService.updateRole(userId, kbId, memberId, request.getRole()));
    }

    @Operation(summary = "移除成员")
    @DeleteMapping("/{memberId}")
    public Result<Void> remove(@PathVariable Long kbId, @PathVariable Long memberId) {
        Long userId = CurrentUserHolder.requireUserId();
        kbMemberService.remove(userId, kbId, memberId);
        return Result.success();
    }

    @Data
    public static class RoleUpdateRequest {
        @NotBlank
        private String role;
    }
}
