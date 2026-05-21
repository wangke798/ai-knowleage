package com.smartdocs.aikb.module.kb.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.kb.dto.KbSearchHit;
import com.smartdocs.aikb.module.kb.service.DocumentRetrievalService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库语义检索（调试/RAG 召回接口）。
 */
@Validated
@RestController
@RequestMapping("/kb/{kbId}")
@RequiredArgsConstructor
public class KbSearchController {

    private final DocumentRetrievalService retrievalService;

    @GetMapping("/search")
    public Result<List<KbSearchHit>> search(@PathVariable Long kbId,
                                            @RequestParam @NotBlank String q,
                                            @RequestParam(defaultValue = "5") int topK,
                                            @RequestParam(required = false) Double threshold) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(retrievalService.search(userId, kbId, q, topK, threshold));
    }
}
