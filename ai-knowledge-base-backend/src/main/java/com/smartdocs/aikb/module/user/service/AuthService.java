package com.smartdocs.aikb.module.user.service;

import java.util.Map;

public interface AuthService {

    Map<String, Object> register(Map<String, Object> params);

    Map<String, Object> login(Map<String, Object> params);

    Map<String, Object> refresh(String refreshToken);

    void logout(String accessToken, String refreshToken);

    Map<String, Object> me(Long userId);
}
