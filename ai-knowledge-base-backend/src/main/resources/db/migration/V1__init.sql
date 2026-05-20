-- ====================================================
-- V1__init.sql  AI 知识库数据库初始化脚本
-- ====================================================

-- -------------------- 用户与权限 --------------------

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL COMMENT '主键',
    username    VARCHAR(64)  NOT NULL COMMENT '用户名',
    password    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）',
    nickname    VARCHAR(64)           COMMENT '昵称',
    avatar      VARCHAR(512)          COMMENT '头像 URL',
    email       VARCHAR(128)          COMMENT '邮箱',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL COMMENT '主键',
    code        VARCHAR(64)  NOT NULL COMMENT '角色编码',
    name        VARCHAR(64)  NOT NULL COMMENT '角色名称',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- -------------------- 知识库 --------------------

CREATE TABLE IF NOT EXISTS kb_knowledge_base (
    id              BIGINT       NOT NULL COMMENT '主键',
    name            VARCHAR(128) NOT NULL COMMENT '知识库名称',
    description     VARCHAR(512)          COMMENT '描述',
    owner_id        BIGINT       NOT NULL COMMENT '创建者 ID',
    embedding_model VARCHAR(64)           COMMENT 'Embedding 模型',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库主表';

CREATE TABLE IF NOT EXISTS kb_member (
    id      BIGINT      NOT NULL,
    kb_id   BIGINT      NOT NULL COMMENT '知识库 ID',
    user_id BIGINT      NOT NULL COMMENT '成员用户 ID',
    role    VARCHAR(32) NOT NULL DEFAULT 'VIEWER' COMMENT '角色：OWNER/EDITOR/VIEWER',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kb_user (kb_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库成员表';

-- -------------------- 文档 --------------------

CREATE TABLE IF NOT EXISTS kb_document (
    id            BIGINT       NOT NULL COMMENT '主键',
    kb_id         BIGINT       NOT NULL COMMENT '知识库 ID',
    name          VARCHAR(255) NOT NULL COMMENT '文件名',
    storage_path  VARCHAR(512) NOT NULL COMMENT 'MinIO 存储路径',
    file_size     BIGINT                COMMENT '文件大小（字节）',
    mime_type     VARCHAR(128)          COMMENT 'MIME 类型',
    file_hash     VARCHAR(64)           COMMENT 'SHA-256 文件指纹（去重）',
    version       INT          NOT NULL DEFAULT 1 COMMENT '版本号',
    parent_doc_id BIGINT                COMMENT '父文档 ID',
    parse_status  VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT '解析状态',
    parse_error   TEXT                  COMMENT '解析失败原因',
    chunk_count   INT                   COMMENT '分块数量',
    uploader_id   BIGINT       NOT NULL COMMENT '上传者 ID',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_kb_id (kb_id),
    KEY idx_file_hash (file_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档元数据表';

-- -------------------- 对话 --------------------

CREATE TABLE IF NOT EXISTS chat_conversation (
    id          BIGINT       NOT NULL COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '用户 ID',
    kb_id       BIGINT       NOT NULL COMMENT '知识库 ID',
    title       VARCHAR(255)          COMMENT '会话标题',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_kb_id (kb_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话表';

CREATE TABLE IF NOT EXISTS chat_message (
    id                  BIGINT       NOT NULL COMMENT '主键',
    conversation_id     BIGINT       NOT NULL COMMENT '会话 ID',
    role                VARCHAR(16)  NOT NULL COMMENT '角色：USER/ASSISTANT',
    content             LONGTEXT     NOT NULL COMMENT '消息内容',
    citation_chunk_ids  TEXT                  COMMENT '引用 chunk IDs（JSON 数组）',
    token_count         INT                   COMMENT '消耗 Token 数',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话消息表';

-- -------------------- 系统配置 --------------------

CREATE TABLE IF NOT EXISTS sys_model_config (
    id          BIGINT       NOT NULL COMMENT '主键',
    name        VARCHAR(64)  NOT NULL COMMENT '配置名称',
    provider    VARCHAR(32)  NOT NULL COMMENT '提供商：OPENAI/OLLAMA/TONGYI',
    model_type  VARCHAR(16)  NOT NULL COMMENT '类型：LLM/EMBEDDING',
    model_name  VARCHAR(128) NOT NULL COMMENT '模型名称',
    api_key     VARCHAR(512)          COMMENT 'API Key（加密存储）',
    base_url    VARCHAR(256)          COMMENT 'API Base URL',
    is_default  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置表';

-- -------------------- 初始数据 --------------------

-- 默认角色
INSERT IGNORE INTO sys_role (id, code, name) VALUES
    (1, 'ADMIN', '管理员'),
    (2, 'USER',  '普通用户');
