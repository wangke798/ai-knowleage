package com.smartdocs.aikb.module.kb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档切片。对应表 {@code kb_document_chunk}。
 */
@Data
@TableName("kb_document_chunk")
public class KbDocumentChunk {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long docId;

    private Long kbId;

    /** 块序号，从 0 开始 */
    private Integer seq;

    private String content;

    private Integer charCount;

    private Integer tokenCount;

    /** 外部向量库记录 ID（Phase 3.3 写入） */
    private String vectorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
