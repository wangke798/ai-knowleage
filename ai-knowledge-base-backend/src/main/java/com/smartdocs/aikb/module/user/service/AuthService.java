package com.smartdocs.aikb.module.user.service;

import com.smartdocs.aikb.module.user.dto.LoginRequest;
import com.smartdocs.aikb.module.user.dto.LoginResponse;
import com.smartdocs.aikb.module.user.dto.RegisterRequest;
import com.smartdocs.aikb.module.user.dto.UserInfoVO;

public interface AuthService {

    UserInfoVO register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    /** 用 refreshToken 换新的 accessToken（不轮换 refreshToken）。 */
    LoginResponse refresh(String refreshToken);

    /** 登出：把当前 access token 的 jti 加入 Redis 黑名单，若提供 refresh 也一并加入。 */
    void logout(String accessToken, String refreshToken);

    UserInfoVO me(Long userId);
}
