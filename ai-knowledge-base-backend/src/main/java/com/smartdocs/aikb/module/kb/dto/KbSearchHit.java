package com.smartdocs.aikb.module.kb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库检索单条结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KbSearchHit {

    /** 命中片段内容 */
    private String content;

    /** 相似度分数（0~1，越大越相关） */
    private Double score;

    private Long kbId;
    private Long docId;
    private Long chunkId;
    private Integer seq;
    private String docName;
}
