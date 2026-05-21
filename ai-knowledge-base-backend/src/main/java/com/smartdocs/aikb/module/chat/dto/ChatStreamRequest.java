package com.smartdocs.aikb.module.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发起一次 RAG 流式对话。
 *
 * <ul>
 *   <li>{@code conversationId} 为空时，后端会按 {@code kbId} 自动新建会话并写入 title。</li>
 *   <li>{@code conversationId} 非空时，{@code kbId} 可省略，从会话取。</li>
 * </ul>
 */
@Data
public class ChatStreamRequest {

    private Long conversationId;

    private Long kbId;

    @NotBlank
    @Size(max = 4000)
    private String question;

    /** 检索 topK，默认 5 */
    private Integer topK;

    /** 相似度阈值（0~1），可空 */
    private Double similarityThreshold;
}
