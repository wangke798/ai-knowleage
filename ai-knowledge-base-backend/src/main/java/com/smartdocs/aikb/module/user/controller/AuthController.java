package com.smartdocs.aikb.module.user.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.module.user.dto.LoginRequest;
import com.smartdocs.aikb.module.user.dto.LoginResponse;
import com.smartdocs.aikb.module.user.dto.RefreshRequest;
import com.smartdocs.aikb.module.user.dto.RegisterRequest;
import com.smartdocs.aikb.module.user.dto.UserInfoVO;
import com.smartdocs.aikb.module.user.service.AuthService;
import com.smartdocs.aikb.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE = "refresh_token";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    private final AuthService authService;

    @Value("${app.jwt.refresh-token-expire:604800}")
    private int refreshTtlSeconds;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Operation(summary = "注册账号")
    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse data = authService.login(request);
        writeRefreshCookie(response, data.getRefreshToken());
        return Result.success(data);
    }

    @Operation(summary = "刷新 Access Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody(required = false) RefreshRequest body,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(body, request);
        LoginResponse data = authService.refresh(refreshToken);
        writeRefreshCookie(response, data.getRefreshToken());
        return Result.success(data);
    }

    @Operation(summary = "登出（吊销当前 Token）")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String access = resolveAccessToken(request);
        String refresh = resolveCookieValue(request, REFRESH_COOKIE);
        authService.logout(access, refresh);
        clearRefreshCookie(response);
        return Result.success();
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public Result<UserInfoVO> me(@AuthenticationPrincipal AuthUser principal) {
        return Result.success(authService.me(principal.getUserId()));
    }

    // ---- helpers ----

    private String resolveRefreshToken(RefreshRequest body, HttpServletRequest request) {
        if (body != null && StringUtils.hasText(body.getRefreshToken())) {
            return body.getRefreshToken();
        }
        return resolveCookieValue(request, REFRESH_COOKIE);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER)) {
            return header.substring(BEARER.length()).trim();
        }
        return null;
    }

    private String resolveCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private void writeRefreshCookie(HttpServletResponse response, String refreshToken) {
        // 直接拼装以支持 SameSite=Lax；ResponseCookie 在 Spring 6 中更优雅但需要 ResponseEntity 配合
        StringBuilder sb = new StringBuilder();
        sb.append(REFRESH_COOKIE).append('=').append(refreshToken == null ? "" : refreshToken);
        sb.append("; Path=/api/auth");
        sb.append("; Max-Age=").append(refreshTtlSeconds);
        sb.append("; HttpOnly");
        sb.append("; SameSite=Lax");
        if (cookieSecure) {
            sb.append("; Secure");
        }
        response.addHeader("Set-Cookie", sb.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append(REFRESH_COOKIE).append("=");
        sb.append("; Path=/api/auth");
        sb.append("; Max-Age=0");
        sb.append("; HttpOnly");
        sb.append("; SameSite=Lax");
        if (cookieSecure) {
            sb.append("; Secure");
        }
        response.addHeader("Set-Cookie", sb.toString());
    }
}
