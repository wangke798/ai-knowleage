package com.smartdocs.aikb.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 把 Security 未认证 / 无权限的响应统一为 {@link Result} JSON。
 */
@Component
@RequiredArgsConstructor
public class JsonAuthFailureHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        write(response, HttpServletResponse.SC_UNAUTHORIZED, ResultCode.UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, int status, ResultCode code) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Void> body = Result.fail(code);
        body.setTraceId(MDC.get("traceId"));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
