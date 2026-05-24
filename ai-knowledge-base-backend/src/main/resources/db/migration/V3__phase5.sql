-- ====================================================
-- V3__phase5.sql  Phase 5 (idempotent)
-- ====================================================

SET @v3_col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_conversation' AND COLUMN_NAME = 'is_favorite');
SET @v3_sql = IF(@v3_col = 0, 'ALTER TABLE chat_conversation ADD COLUMN is_favorite TINYINT NOT NULL DEFAULT 0 AFTER title', 'SELECT 1');
PREPARE v3_stmt FROM @v3_sql; EXECUTE v3_stmt; DEALLOCATE PREPARE v3_stmt;

SET @v3_idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_conversation' AND INDEX_NAME = 'idx_user_favorite');
SET @v3_sql = IF(@v3_idx = 0, 'ALTER TABLE chat_conversation ADD INDEX idx_user_favorite (user_id, is_favorite)', 'SELECT 1');
PREPARE v3_stmt FROM @v3_sql; EXECUTE v3_stmt; DEALLOCATE PREPARE v3_stmt;

CREATE TABLE IF NOT EXISTS sys_model_config (
    id          BIGINT       NOT NULL COMMENT '主键',
    model_type  VARCHAR(32)  NOT NULL COMMENT '类型:CHAT/EMBEDDING',
    provider    VARCHAR(32)  NOT NULL COMMENT '提供商:OPENAI/OLLAMA/DASHSCOPE',
    model_name  VARCHAR(128) NOT NULL COMMENT '模型名称',
    api_key     VARCHAR(512)          COMMENT 'API Key',
    base_url    VARCHAR(512)          COMMENT '接入 URL',
    is_default  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认',
    enabled     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
    description VARCHAR(255)          COMMENT '备注',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_model_type (model_type),
    KEY idx_provider (provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置表';

SET @v3_c1 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_model_config' AND COLUMN_NAME = 'api_key');
SET @v3_sql = IF(@v3_c1 = 0, 'ALTER TABLE sys_model_config ADD COLUMN api_key VARCHAR(512) AFTER model_name', 'SELECT 1');
PREPARE v3_stmt FROM @v3_sql; EXECUTE v3_stmt; DEALLOCATE PREPARE v3_stmt;

SET @v3_c2 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_model_config' AND COLUMN_NAME = 'base_url');
SET @v3_sql = IF(@v3_c2 = 0, 'ALTER TABLE sys_model_config ADD COLUMN base_url VARCHAR(512) AFTER api_key', 'SELECT 1');
PREPARE v3_stmt FROM @v3_sql; EXECUTE v3_stmt; DEALLOCATE PREPARE v3_stmt;

SET @v3_c3 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_model_config' AND COLUMN_NAME = 'is_default');
SET @v3_sql = IF(@v3_c3 = 0, 'ALTER TABLE sys_model_config ADD COLUMN is_default TINYINT NOT NULL DEFAULT 0 AFTER base_url', 'SELECT 1');
PREPARE v3_stmt FROM @v3_sql; EXECUTE v3_stmt; DEALLOCATE PREPARE v3_stmt;

SET @v3_c4 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_model_config' AND COLUMN_NAME = 'enabled');
SET @v3_sql = IF(@v3_c4 = 0, 'ALTER TABLE sys_model_config ADD COLUMN enabled TINYINT NOT NULL DEFAULT 1 AFTER is_default', 'SELECT 1');
PREPARE v3_stmt FROM @v3_sql; EXECUTE v3_stmt; DEALLOCATE PREPARE v3_stmt;

SET @v3_c5 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_model_config' AND COLUMN_NAME = 'description');
SET @v3_sql = IF(@v3_c5 = 0, 'ALTER TABLE sys_model_config ADD COLUMN description VARCHAR(255) AFTER enabled', 'SELECT 1');
PREPARE v3_stmt FROM @v3_sql; EXECUTE v3_stmt; DEALLOCATE PREPARE v3_stmt;

INSERT IGNORE INTO sys_model_config (id, model_type, provider, model_name, base_url, is_default, enabled, description)
VALUES
    (1000000000000000001, 'CHAT',      'DASHSCOPE', 'qwen-plus',         'https://dashscope.aliyuncs.com/compatible-mode', 1, 1, 'QianWen Plus'),
    (1000000000000000002, 'EMBEDDING', 'DASHSCOPE', 'text-embedding-v3', 'https://dashscope.aliyuncs.com/compatible-mode', 1, 1, 'QianWen Embedding V3'),
    (1000000000000000003, 'CHAT',      'OLLAMA',    'qwen2.5:7b',         'http://localhost:11434', 0, 1, 'Ollama Qwen2.5 7B'),
    (1000000000000000004, 'EMBEDDING', 'OLLAMA',    'bge-m3',             'http://localhost:11434', 0, 1, 'Ollama bge-m3');
