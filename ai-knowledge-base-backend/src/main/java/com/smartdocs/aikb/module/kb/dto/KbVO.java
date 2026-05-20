package com.smartdocs.aikb.module.kb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "知识库视图对象")
public class KbVO {

    private Long id;

    private String name;

    private String description;

    private Long ownerId;

    private String ownerName;

    private String embeddingModel;

    private Integer status;

    @Schema(description = "当前用户在该知识库中的角色：OWNER/EDITOR/VIEWER")
    private String currentUserRole;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
