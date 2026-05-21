package com.smartdocs.aikb.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {
    private Long id;
    private Long kbId;
    private String kbName;
    private String title;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
