package com.smartdocs.aikb.module.kb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档元数据。对应表 {@code kb_document}。
 */
@Data
@TableName("kb_document")
public class KbDocument {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属知识库 ID */
    private Long kbId;

    /** 原始文件名 */
    private String name;

    /** 存储路径（相对存储根目录） */
    private String storagePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MIME 类型 */
    private String mimeType;

    /** SHA-256 文件指纹，用于同知识库内去重 */
    private String fileHash;

    /** 版本号，默认 1 */
    private Integer version;

    /** 父文档 ID（用于版本演进，暂未启用） */
    private Long parentDocId;

    /** 解析状态：PENDING / PROCESSING / DONE / FAILED */
    private String parseStatus;

    /** 解析失败原因 */
    private String parseError;

    /** 切片数量 */
    private Integer chunkCount;

    /** 上传者用户 ID */
    private Long uploaderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
