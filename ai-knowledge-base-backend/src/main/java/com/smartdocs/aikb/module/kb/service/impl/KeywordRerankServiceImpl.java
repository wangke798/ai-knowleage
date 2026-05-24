package com.smartdocs.aikb.module.kb.service.impl;

import com.smartdocs.aikb.module.kb.service.RerankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 基于关键词覆盖率 + 向量相似度的混合重排序实现。
 *
 * <p>评分公式：
 * <pre>
 *   finalScore = vectorScore * 0.6 + keywordScore * 0.4
 * </pre>
 * keywordScore = 查询中有多少词在片段中出现 / 查询词总数（[0,1]）
 *
 * <p>无需部署额外模型即可提升排序质量，尤其对中文技术文档效果较好。
 * 如需更强效果，可替换为 Ollama bge-reranker 实现（调用 /api/embed 接口）。
 */
@Slf4j
@Service
public class KeywordRerankServiceImpl implements RerankService {

    /** 将查询切成词（支持中文字符按字切分，英文按空格切）。 */
    private static final Pattern WORD_SEP = Pattern.compile("[\\s\\p{Punct}\\uff0c\\u3002\\uff01\\uff1f\\uff1b\\uff1a\\u3001\\u201c\\u201d\\u2018\\u2019\\u3010\\u3011\\u300c\\u300d\\u2026\\u2014]+");

    @Override
    public List<Map<String, Object>> rerank(String query, List<Map<String, Object>> hits) {
        if (hits == null || hits.isEmpty()) return List.of();
        if (query == null || query.isBlank()) return hits;

        Set<String> queryTokens = tokenize(query);
        if (queryTokens.isEmpty()) return hits;

        List<Map<String, Object>> scored = new ArrayList<>(hits.size());
        for (Map<String, Object> hit : hits) {
            double vectorScore = toDouble(hit.get("score"), 0.0);
            String content = (String) hit.get("content");
            double kwScore = (content == null) ? 0.0 : keywordScore(queryTokens, content);
            double finalScore = vectorScore * 0.6 + kwScore * 0.4;

            Map<String, Object> copy = new LinkedHashMap<>(hit);
            copy.put("score", finalScore);
            copy.put("vectorScore", vectorScore);
            copy.put("kwScore", kwScore);
            scored.add(copy);
        }

        scored.sort(Comparator.comparingDouble(m -> -toDouble(m.get("score"), 0.0)));
        log.debug("[Rerank] query='{}' hits={} -> reranked top3scores={}",
                query.length() > 30 ? query.substring(0, 30) + "…" : query,
                hits.size(),
                scored.stream().limit(3).map(m -> String.format("%.4f", toDouble(m.get("score"), 0.0))).toList());
        return scored;
    }

    // ─── helpers ───

    /** 将文本分词（简单策略：分隔符切分 + 中文单字切分）。 */
    private static Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        String[] parts = WORD_SEP.split(text.toLowerCase());
        for (String p : parts) {
            if (p.isBlank()) continue;
            // 英文整词
            if (p.chars().allMatch(c -> c < 128)) {
                if (p.length() >= 2) tokens.add(p);
            } else {
                // 中文：整词 + 每个字
                tokens.add(p);
                p.chars().forEach(c -> {
                    if (c >= 0x4E00 && c <= 0x9FFF) {
                        tokens.add(String.valueOf((char) c));
                    }
                });
            }
        }
        return tokens;
    }

    /** 计算查询词在片段中的覆盖率（[0,1]）。 */
    private static double keywordScore(Set<String> queryTokens, String content) {
        if (queryTokens.isEmpty()) return 0.0;
        String lower = content.toLowerCase();
        long hits = queryTokens.stream().filter(lower::contains).count();
        return (double) hits / queryTokens.size();
    }

    private static double toDouble(Object v, double def) {
        if (v == null) return def;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return def; }
    }
}
