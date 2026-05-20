package com.smartdocs.aikb.module.kb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "添加/更新知识库成员请求体")
public class KbMemberRequest {

    @NotNull(message = "userId 不能为空")
    private Long userId;

    @NotNull(message = "角色不能为空")
    @Pattern(regexp = "OWNER|EDITOR|VIEWER", message = "角色仅支持 OWNER/EDITOR/VIEWER")
    @Schema(description = "角色：OWNER/EDITOR/VIEWER", example = "VIEWER")
    private String role;
}
