package com.smartdocs.aikb.module.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConversationRenameRequest {

    @NotBlank
    @Size(max = 100)
    private String title;
}
