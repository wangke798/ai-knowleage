package com.smartdocs.aikb.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {
    private Long id;
    private Long conversationId;
    private String role; // USER / ASSISTANT
    private String content;
    /** 引用片段（仅 ASSISTANT 消息有） */
    private List<CitationVO> citations;
    private Integer tokenCount;
    private LocalDateTime createTime;
}
