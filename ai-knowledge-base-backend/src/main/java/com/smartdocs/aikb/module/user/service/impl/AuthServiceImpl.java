package com.smartdocs.aikb.module.user.service.impl;

import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.module.user.entity.SysUser;
import com.smartdocs.aikb.module.user.mapper.SysUserMapper;
import com.smartdocs.aikb.module.user.service.AuthService;
import com.smartdocs.aikb.security.JwtAuthenticationFilter;
import com.smartdocs.aikb.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Map<String, Object> register(Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String nickname = (String) params.get("nickname");
        String email    = (String) params.get("email");
        if (!org.springframework.util.StringUtils.hasText(username)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "\u7528\u6237\u540d\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (!org.springframework.util.StringUtils.hasText(password)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (sysUserMapper.selectByUsername(username) != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(StringUtils.hasText(nickname) ? nickname : username);
        user.setEmail(email);
        user.setStatus(1);
        sysUserMapper.insert(user);
        return toUserInfo(user);
    }

    @Override
    public Map<String, Object> login(Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_INCORRECT, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        return buildLoginResponse(user);
    }

    @Override
    public Map<String, Object> refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        JwtTokenProvider.ParsedToken parsed;
        try {
            parsed = jwtTokenProvider.parse(refreshToken);
        } catch (JwtException e) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID, "Refresh Token 无效");
        }
        if (!JwtTokenProvider.TYPE_REFRESH.equals(parsed.type())) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID, "Token 类型错误");
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(JwtAuthenticationFilter.BLACKLIST_KEY_PREFIX + parsed.jti()))) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID, "Refresh Token 已失效");
        }
        SysUser user = sysUserMapper.selectById(parsed.userId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 只换新 access，不轮换 refresh（避免实现重放检测的复杂度）
        JwtTokenProvider.TokenPayload access = jwtTokenProvider.issueAccess(user.getId(), user.getUsername());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("accessToken", access.token());
        resp.put("accessTokenExpireAt", access.expireAtMillis());
        resp.put("refreshToken", refreshToken);
        resp.put("refreshTokenExpireAt", parsed.expireAtMillis());
        resp.put("user", toUserInfo(user));
        return resp;
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        blacklistIfPresent(accessToken);
        blacklistIfPresent(refreshToken);
    }

    @Override
    public Map<String, Object> me(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toUserInfo(user);
    }

    // ------------------------- helpers -------------------------

    private Map<String, Object> buildLoginResponse(SysUser user) {
        JwtTokenProvider.TokenPayload access = jwtTokenProvider.issueAccess(user.getId(), user.getUsername());
        JwtTokenProvider.TokenPayload refresh = jwtTokenProvider.issueRefresh(user.getId(), user.getUsername());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("accessToken", access.token());
        resp.put("accessTokenExpireAt", access.expireAtMillis());
        resp.put("refreshToken", refresh.token());
        resp.put("refreshTokenExpireAt", refresh.expireAtMillis());
        resp.put("user", toUserInfo(user));
        return resp;
    }

    private Map<String, Object> toUserInfo(SysUser user) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("nickname", StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        m.put("email", user.getEmail());
        m.put("status", user.getStatus());
        return m;
    }

    private void blacklistIfPresent(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            JwtTokenProvider.ParsedToken parsed = jwtTokenProvider.parse(token);
            long remainMillis = parsed.expireAtMillis() - Instant.now().toEpochMilli();
            if (remainMillis <= 0) {
                return; // 已过期无需拉黑
            }
            redisTemplate.opsForValue().set(
                    JwtAuthenticationFilter.BLACKLIST_KEY_PREFIX + parsed.jti(),
                    "1",
                    Duration.ofMillis(remainMillis));
        } catch (JwtException e) {
            log.debug("登出时遇到无效 Token，已忽略：{}", e.getMessage());
        }
    }
}
