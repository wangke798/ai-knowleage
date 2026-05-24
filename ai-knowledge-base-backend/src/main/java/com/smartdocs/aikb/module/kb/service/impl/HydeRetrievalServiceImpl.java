package com.smartdocs.aikb.module.kb.service.impl;

import com.smartdocs.aikb.module.kb.service.DocumentRetrievalService;
import com.smartdocs.aikb.module.kb.service.HydeRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * HyDE 实现：调用 ChatClient 生成假设答案后再检索。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HydeRetrievalServiceImpl implements HydeRetrievalService {

    private static final String HYDE_SYSTEM_PROMPT = """
            你是一个知识库检索助手。
            请根据用户问题，写一段简短但信息丰富的"假设答案"（50～120 字），
            用于后续向量检索。不要注明"假设"字样，直接写答案内容，不要废话。
            """;

    private final ChatClient chatClient;
    private final DocumentRetrievalService retrievalService;

    @Override
    public List<Map<String, Object>> searchWithHyde(Long userId, Long kbId, String question,
                                                    int topK, Double threshold) {
        // 1) 生成假设答案
        String hypotheticalAnswer = generateHypotheticalAnswer(question);
        log.debug("[HyDE] question='{}' → hypothetical='{}'",
                truncate(question, 40), truncate(hypotheticalAnswer, 60));

        // 2) 用假设答案检索
        List<Map<String, Object>> hydeHits = retrievalService.search(
                userId, kbId, hypotheticalAnswer, topK, threshold);

        // 3) 同时用原始问题检索（提升召回率）
        List<Map<String, Object>> origHits = retrievalService.search(
                userId, kbId, question, topK, threshold);

        // 4) 合并去重（按 chunkId），保留较高分数的那一条
        return mergeDedup(hydeHits, origHits, topK);
    }

    // ─── private helpers ───

    private String generateHypotheticalAnswer(String question) {
        try {
            return chatClient.prompt()
                    .system(HYDE_SYSTEM_PROMPT)
                    .user(question)
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("[HyDE] LLM 调用失败，降级为原始查询: {}", e.getMessage());
            return question;
        }
    }

    private static List<Map<String, Object>> mergeDedup(
            List<Map<String, Object>> primary,
            List<Map<String, Object>> secondary,
            int topK) {
        Map<Object, Map<String, Object>> merged = new LinkedHashMap<>();
        // primary 优先
        for (Map<String, Object> h : primary) {
            Object key = h.get("chunkId");
            if (key == null) key = h.get("content");
            merged.put(key, h);
        }
        // secondary 补充或取较高分
        for (Map<String, Object> h : secondary) {
            Object key = h.get("chunkId");
            if (key == null) key = h.get("content");
            if (!merged.containsKey(key)) {
                merged.put(key, h);
            } else {
                // 保留较高的向量分
                double existScore = toDouble(merged.get(key).get("score"), 0.0);
                double newScore   = toDouble(h.get("score"), 0.0);
                if (newScore > existScore) {
                    merged.put(key, h);
                }
            }
        }
        // 按分数排序后截断
        return merged.values().stream()
                .sorted(Comparator.comparingDouble(m -> -toDouble(m.get("score"), 0.0)))
                .limit(topK)
                .toList();
    }

    private static double toDouble(Object v, double def) {
        if (v == null) return def;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return def; }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
