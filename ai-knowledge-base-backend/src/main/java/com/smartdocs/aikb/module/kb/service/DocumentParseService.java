package com.smartdocs.aikb.module.kb.service;

/**
 * 文档解析与切片。Phase 3.2：抽取文本 → 字符滑窗切片。
 * Phase 3.3 在此基础上追加向量化。
 */
public interface DocumentParseService {

    /**
     * 异步执行解析。会更新 {@code kb_document.parse_status / chunk_count / parse_error}
     * 并写入 {@code kb_document_chunk}。失败被吞并打日志，不向上抛。
     */
    void parseAsync(Long docId);
}
