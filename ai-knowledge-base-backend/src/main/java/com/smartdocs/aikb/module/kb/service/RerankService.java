package com.smartdocs.aikb.module.kb.service;

import java.util.List;
import java.util.Map;

/**
 * 文档重排序服务。
 * <p>对向量检索结果进行二次打分，使相关度高的片段排在前面。
 * 默认实现：关键词 + 向量得分加权混合排序（无需额外模型）。
 */
public interface RerankService {

    /**
     * 对检索结果重排序。
     *
     * @param query  原始用户查询
     * @param hits   向量检索返回的片段，每个 Map 含 content / score / chunkId 等字段
     * @return 重排后的列表（高相关在前）
     */
    List<Map<String, Object>> rerank(String query, List<Map<String, Object>> hits);
}
