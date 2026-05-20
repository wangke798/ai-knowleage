package com.smartdocs.aikb.module.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long conversationId;

    /** 角色：USER / ASSISTANT */
    private String role;

    private String content;

    /** 引用的 chunk ids（JSON 数组字符串） */
    private String citationChunkIds;

    /** 消耗 Token 数 */
    private Integer tokenCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
