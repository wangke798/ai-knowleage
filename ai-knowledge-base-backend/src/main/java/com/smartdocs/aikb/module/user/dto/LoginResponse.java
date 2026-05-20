package com.smartdocs.aikb.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应体")
public class LoginResponse {

    private String accessToken;

    @Schema(description = "Access Token 过期时间戳（毫秒）")
    private Long accessTokenExpireAt;

    /**
     * Refresh Token：仅在响应体里返回一次，方便接口测试与 Web 端把它写入 HttpOnly Cookie。
     * 生产部署可改为直接由后端 Set-Cookie 而不在 body 中返回。
     */
    private String refreshToken;

    private Long refreshTokenExpireAt;

    private UserInfoVO user;
}
