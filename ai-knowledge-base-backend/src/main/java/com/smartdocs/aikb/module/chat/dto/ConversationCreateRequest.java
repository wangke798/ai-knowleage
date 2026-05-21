package com.smartdocs.aikb.module.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建会话请求。
 */
@Data
public class ConversationCreateRequest {

    @NotNull
    private Long kbId;

    @Size(max = 100)
    private String title;
}
