package com.smartdocs.aikb.module.kb.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.kb.service.KbMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kb/{kbId}/members")
@RequiredArgsConstructor
public class KbMemberController {

    private final KbMemberService kbMemberService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@PathVariable Long kbId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(kbMemberService.list(userId, kbId));
    }

    @PostMapping
    public Result<Map<String, Object>> add(@PathVariable Long kbId, @RequestBody Map<String, Object> params) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(kbMemberService.add(userId, kbId, params));
    }

    @PutMapping("/{memberId}")
    public Result<Map<String, Object>> updateRole(@PathVariable Long kbId,
                                                  @PathVariable Long memberId,
                                                  @RequestBody Map<String, Object> params) {
        Long userId = CurrentUserHolder.requireUserId();
        String role = (String) params.get("role");
        return Result.success(kbMemberService.updateRole(userId, kbId, memberId, role));
    }

    @DeleteMapping("/{memberId}")
    public Result<Void> remove(@PathVariable Long kbId, @PathVariable Long memberId) {
        Long userId = CurrentUserHolder.requireUserId();
        kbMemberService.remove(userId, kbId, memberId);
        return Result.success();
    }
}
