package com.smartdocs.aikb.module.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_conversation")
public class ChatConversation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long kbId;

    private String title;

    /** 是否收藏：0-否 1-是 */
    private Integer isFavorite;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
