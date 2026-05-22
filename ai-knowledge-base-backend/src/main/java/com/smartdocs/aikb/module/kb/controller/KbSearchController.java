package com.smartdocs.aikb.module.kb.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.kb.service.DocumentRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kb/{kbId}")
@RequiredArgsConstructor
public class KbSearchController {

    private final DocumentRetrievalService retrievalService;

    @GetMapping("/search")
    public Result<List<Map<String, Object>>> search(@PathVariable Long kbId,
                                                    @RequestParam String q,
                                                    @RequestParam(defaultValue = "5") int topK,
                                                    @RequestParam(required = false) Double threshold) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(retrievalService.search(userId, kbId, q, topK, threshold));
    }
}
