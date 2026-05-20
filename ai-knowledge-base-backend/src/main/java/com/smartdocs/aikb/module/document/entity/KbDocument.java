package com.smartdocs.aikb.module.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_document")
public class KbDocument {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long kbId;

    private String name;

    /** MinIO 存储路径 */
    private String storagePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MIME 类型 */
    private String mimeType;

    /** SHA-256 文件指纹（去重用） */
    private String fileHash;

    /** 文档版本号 */
    private Integer version;

    /** 父文档 ID（更新版本时关联） */
    private Long parentDocId;

    /**
     * 解析状态：
     * PENDING    - 待解析
     * PROCESSING - 解析中
     * DONE       - 已完成
     * FAILED     - 失败
     */
    private String parseStatus;

    /** 解析失败原因 */
    private String parseError;

    /** 分块数量 */
    private Integer chunkCount;

    private Long uploaderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
