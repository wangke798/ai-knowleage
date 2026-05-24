package com.smartdocs.aikb.module.kb.service;

import java.util.List;
import java.util.Map;

/**
 * HyDE（Hypothetical Document Embeddings）检索增强。
 *
 * <p>策略：
 * <ol>
 *   <li>调用小型 LLM 生成一段"假设答案"（不依赖知识库，只根据问题推测）</li>
 *   <li>将假设答案 embedding，用于向量检索（替代原始查询 embedding）</li>
 *   <li>再用原始查询做一次检索，取并集去重（提升召回率）</li>
 * </ol>
 */
public interface HydeRetrievalService {

    /**
     * 通过 HyDE 增强检索：先生成假设答案，再用假设答案检索向量库。
     *
     * @param userId    当前用户 ID（权限校验用）
     * @param kbId      知识库 ID
     * @param question  用户原始问题
     * @param topK      返回条数
     * @param threshold 相似度阈值
     * @return 去重合并后的检索结果（高分在前）
     */
    List<Map<String, Object>> searchWithHyde(Long userId, Long kbId, String question,
                                             int topK, Double threshold);
}
