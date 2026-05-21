package com.smartdocs.aikb.module.kb.service;

import com.smartdocs.aikb.module.kb.dto.KbSearchHit;

import java.util.List;

/**
 * 知识库语义检索。
 */
public interface DocumentRetrievalService {

    /**
     * 在指定知识库内按 query 做相似度检索。
     *
     * @param userId    当前用户（用于权限校验）
     * @param kbId      限定知识库
     * @param query     查询语句
     * @param topK      返回前 K 条
     * @param threshold 相似度阈值（0~1，命中需 score >= threshold；为 null 时不过滤）
     */
    List<KbSearchHit> search(Long userId, Long kbId, String query, int topK, Double threshold);
}
