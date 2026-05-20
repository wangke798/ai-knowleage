package com.smartdocs.aikb.module.kb.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档展示 VO
 */
@Data
public class KbDocumentVO {

    private Long id;
    private Long kbId;
    private String name;
    private Long fileSize;
    private String mimeType;
    private String fileHash;
    private Integer version;
    private String parseStatus;
    private String parseError;
    private Integer chunkCount;
    private Long uploaderId;
    private String uploaderName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
