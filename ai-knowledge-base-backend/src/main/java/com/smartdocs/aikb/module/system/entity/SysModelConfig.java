package com.smartdocs.aikb.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型配置实体，对应 sys_model_config 表。
 */
@Data
@TableName("sys_model_config")
public class SysModelConfig {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模型类型：CHAT / EMBEDDING */
    private String modelType;

    /** 提供商：OPENAI / OLLAMA / DASHSCOPE */
    private String provider;

    /** 模型名称，如 qwen-plus / bge-m3 */
    private String modelName;

    /** API Key（建议生产环境加密存储） */
    private String apiKey;

    /** 接入 URL */
    private String baseUrl;

    /** 是否默认：0-否 1-是 */
    private Integer isDefault;

    /** 是否启用：0-禁用 1-启用 */
    private Integer enabled;

    /** 备注 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
