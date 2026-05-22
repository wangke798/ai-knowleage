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
}
