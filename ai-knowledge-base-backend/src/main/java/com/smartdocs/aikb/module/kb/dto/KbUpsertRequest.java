package com.smartdocs.aikb.module.kb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建/更新知识库请求体")
public class KbUpsertRequest {

    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 128, message = "知识库名称长度不能超过 128")
    @Schema(description = "知识库名称", example = "产品手册库")
    private String name;

    @Size(max = 512, message = "描述长度不能超过 512")
    @Schema(description = "知识库描述")
    private String description;

    @Size(max = 64)
    @Schema(description = "Embedding 模型，留空使用系统默认", example = "bge-m3")
    private String embeddingModel;
}
