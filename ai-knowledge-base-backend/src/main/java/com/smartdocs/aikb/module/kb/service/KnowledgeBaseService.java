package com.smartdocs.aikb.module.kb.service;

import com.smartdocs.aikb.common.result.PageVO;

import java.util.Map;

public interface KnowledgeBaseService {

    Map<String, Object> create(Long userId, Map<String, Object> params);

    Map<String, Object> update(Long userId, Long kbId, Map<String, Object> params);

    void delete(Long userId, Long kbId);

    Map<String, Object> detail(Long userId, Long kbId);

    PageVO<Map<String, Object>> page(Long userId, long page, long size, String keyword);

    String resolveRole(Long userId, Long kbId);

    /** 获取知识库统计数据：文档数、分块数、会话数、消息数。 */
    Map<String, Object> stats(Long userId, Long kbId);
}
