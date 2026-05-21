package com.smartdocs.aikb.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一条引用 = 一个被检索命中的 chunk 的简要信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitationVO {
    private Long chunkId;
    private Long docId;
    private String docName;
    /** chunk 在文档内的序号 */
    private Integer seq;
    /** 截断后的预览（前 200 字） */
    private String snippet;
    /** 相似度分数 */
    private Double score;
}
