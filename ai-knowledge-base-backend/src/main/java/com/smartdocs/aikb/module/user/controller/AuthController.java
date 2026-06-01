package com.smartdocs.aikb.module.user.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.module.user.service.AuthService;
import com.smartdocs.aikb.security.AuthUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.ResultCode;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE = "refresh_token";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String CAPTCHA_CACHE_PREFIX = "captcha:";

    private final AuthService authService;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.jwt.refresh-token-expire:604800}")
    private int refreshTtlSeconds;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        // 4 位字符验证码（数字+字母），不依赖 Nashorn 脚本引擎，兼容 Java 9+
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(Captcha.TYPE_DEFAULT); // 数字+大小写字母
        String result = captcha.text();
        String base64Image = captcha.toBase64();

        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 设置验证码5分钟有效
        redisTemplate.opsForValue().set(CAPTCHA_CACHE_PREFIX + uuid, result, 5, TimeUnit.MINUTES);

        Map<String, String> data = new HashMap<>();
        data.put("captchaId", uuid);
        data.put("captchaImage", base64Image);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, Object> params) {
        return Result.success(authService.register(params));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> params, HttpServletResponse response) {
        Map<String, Object> data = authService.login(params);
        writeRefreshCookie(response, (String) data.get("refreshToken"));
        return Result.success(data);
    }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestBody(required = false) Map<String, Object> body,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(body, request);
        Map<String, Object> data = authService.refresh(refreshToken);
        writeRefreshCookie(response, (String) data.get("refreshToken"));
        return Result.success(data);
    }


    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String access = resolveAccessToken(request);
        String refresh = resolveCookieValue(request, REFRESH_COOKIE);
        authService.logout(access, refresh);
        clearRefreshCookie(response);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(@AuthenticationPrincipal AuthUser principal) {
        return Result.success(authService.me(principal.getUserId()));
    }

    // ---- helpers ----

    private String resolveRefreshToken(Map<String, Object> body, HttpServletRequest request) {
        if (body != null) {
            Object rt = body.get("refreshToken");
            if (rt instanceof String s && StringUtils.hasText(s)) return s;
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
