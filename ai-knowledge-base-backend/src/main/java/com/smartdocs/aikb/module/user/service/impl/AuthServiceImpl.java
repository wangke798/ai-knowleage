package com.smartdocs.aikb.module.user.service.impl;

import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.module.user.dto.LoginRequest;
import com.smartdocs.aikb.module.user.dto.LoginResponse;
import com.smartdocs.aikb.module.user.dto.RegisterRequest;
import com.smartdocs.aikb.module.user.dto.UserInfoVO;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    public UserInfoVO register(RegisterRequest request) {
        if (sysUserMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        sysUserMapper.insert(user);
        return toUserInfo(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_INCORRECT, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
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

        LoginResponse resp = new LoginResponse();
        resp.setAccessToken(access.token());
        resp.setAccessTokenExpireAt(access.expireAtMillis());
        resp.setRefreshToken(refreshToken);
        resp.setRefreshTokenExpireAt(parsed.expireAtMillis());
        resp.setUser(toUserInfo(user));
        return resp;
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        blacklistIfPresent(accessToken);
        blacklistIfPresent(refreshToken);
    }

    @Override
    public UserInfoVO me(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toUserInfo(user);
    }

    // ------------------------- helpers -------------------------

    private LoginResponse buildLoginResponse(SysUser user) {
        JwtTokenProvider.TokenPayload access = jwtTokenProvider.issueAccess(user.getId(), user.getUsername());
        JwtTokenProvider.TokenPayload refresh = jwtTokenProvider.issueRefresh(user.getId(), user.getUsername());

        LoginResponse resp = new LoginResponse();
        resp.setAccessToken(access.token());
        resp.setAccessTokenExpireAt(access.expireAtMillis());
        resp.setRefreshToken(refresh.token());
        resp.setRefreshTokenExpireAt(refresh.expireAtMillis());
        resp.setUser(toUserInfo(user));
        return resp;
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

    private UserInfoVO toUserInfo(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setRoles(List.of("USER"));
        return vo;
    }
}
