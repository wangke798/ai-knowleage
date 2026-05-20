package com.smartdocs.aikb.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "刷新 Token 请求体（Cookie 与 body 二选一）")
public class RefreshRequest {

    /** 当请求未携带 refresh_token Cookie 时，可在 body 中提供。 */
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
