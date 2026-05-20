package com.smartdocs.aikb.common.exception;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常 [traceId={}]: code={}, message={}", MDC.get("traceId"), e.getCode(), e.getMessage());
        Result<Void> result = Result.fail(e.getCode(), e.getMessage());
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 [traceId={}]: {}", MDC.get("traceId"), message);
        Result<Void> result = Result.fail(ResultCode.BAD_REQUEST, message);
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED);
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        Result<Void> result = Result.fail(ResultCode.FORBIDDEN);
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("未知异常 [traceId={}]", MDC.get("traceId"), e);
        Result<Void> result = Result.fail(ResultCode.INTERNAL_ERROR);
        result.setTraceId(MDC.get("traceId"));
        return result;
    }
}
