package com.smartdocs.aikb.module.kb.service;

import java.util.List;
import java.util.Map;

public interface DocumentRetrievalService {

    List<Map<String, Object>> search(Long userId, Long kbId, String query, int topK, Double threshold);
}
