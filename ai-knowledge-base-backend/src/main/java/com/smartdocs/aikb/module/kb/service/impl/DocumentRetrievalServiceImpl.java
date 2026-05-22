package com.smartdocs.aikb.module.kb.service.impl;

import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.module.kb.service.DocumentRetrievalService;
import com.smartdocs.aikb.module.kb.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentRetrievalServiceImpl implements DocumentRetrievalService {

    private final VectorStore vectorStore;
    private final KnowledgeBaseService knowledgeBaseService;

    @Override
    public List<Map<String, Object>> search(Long userId, Long kbId, String query, int topK, Double threshold) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }
        if (knowledgeBaseService.resolveRole(userId, kbId) == null) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION);
        }
        int k = topK <= 0 ? 5 : Math.min(topK, 50);

        Filter.Expression byKb = new FilterExpressionBuilder()
                .eq("kbId", kbId.toString()).build();

        SearchRequest req = SearchRequest.builder()
                .query(query)
                .topK(k)
                .similarityThreshold(threshold == null ? 0.0d : threshold)
                .filterExpression(byKb)
                .build();

        List<Document> docs;
        try {
            docs = vectorStore.similaritySearch(req);
        } catch (Exception e) {
            log.error("[Retrieval] search failed kbId={} q={}", kbId, query, e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "检索失败：" + safeMsg(e));
        }
        if (docs == null || docs.isEmpty()) return List.of();

        List<Map<String, Object>> hits = new ArrayList<>(docs.size());
        for (Document d : docs) {
            Map<String, Object> meta = d.getMetadata();
            Map<String, Object> hit = new java.util.LinkedHashMap<>();
            hit.put("content", d.getText());
            hit.put("score", d.getScore());
            hit.put("kbId", parseLong(meta.get("kbId")));
            hit.put("docId", parseLong(meta.get("docId")));
            hit.put("chunkId", parseLong(meta.get("chunkId")));
            Object seq = meta.get("seq");
            if (seq instanceof Number n) hit.put("seq", n.intValue());
            else if (seq != null) {
                try { hit.put("seq", Integer.parseInt(seq.toString())); } catch (NumberFormatException ignored) {}
            }
            Object name = meta.get("docName");
            if (name != null) hit.put("docName", name.toString());
            hits.add(hit);
        }
        return hits;
    }

    private static Long parseLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    private static String safeMsg(Throwable e) {
        String msg = e.getMessage();
        return msg == null ? e.getClass().getSimpleName() : msg;
    }
}
