-- ====================================================
-- V2__doc_chunk.sql  文档切片表
-- ====================================================

CREATE TABLE IF NOT EXISTS kb_document_chunk (
    id           BIGINT       NOT NULL COMMENT '主键',
    doc_id       BIGINT       NOT NULL COMMENT '文档 ID',
    kb_id        BIGINT       NOT NULL COMMENT '知识库 ID（冗余，便于按 KB 过滤）',
    seq          INT          NOT NULL COMMENT '块序号，从 0 开始',
    content      MEDIUMTEXT   NOT NULL COMMENT '块文本内容',
    char_count   INT          NOT NULL COMMENT '字符数',
    token_count  INT                   COMMENT '估算 token 数（暂可空）',
    /* 向量在 Phase 3.3 写入外部向量库（PgVector/Milvus），此处仅保留扩展位 */
    vector_id    VARCHAR(64)           COMMENT '外部向量库记录 ID',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_doc_id (doc_id),
    KEY idx_kb_id (kb_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档切片表';
