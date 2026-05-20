package com.smartdocs.aikb.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户名已存在"),
    PASSWORD_INCORRECT(1003, "密码错误"),
    TOKEN_EXPIRED(1004, "Token 已过期"),
    TOKEN_INVALID(1005, "Token 无效"),
    REFRESH_TOKEN_INVALID(1006, "Refresh Token 无效或已过期"),

    // 知识库错误码 2xxx
    KB_NOT_FOUND(2001, "知识库不存在"),
    KB_NO_PERMISSION(2002, "无权限操作该知识库"),
    KB_NAME_DUPLICATE(2003, "同名知识库已存在"),
    KB_MEMBER_EXISTS(2004, "该用户已是知识库成员"),
    KB_MEMBER_NOT_FOUND(2005, "知识库成员不存在"),
    KB_OWNER_CANNOT_REMOVE(2006, "不能移除知识库所有者"),

    // 文档错误码 3xxx
    DOC_NOT_FOUND(3001, "文档不存在"),
    DOC_PARSE_FAILED(3002, "文档解析失败"),
    DOC_TYPE_NOT_SUPPORTED(3003, "不支持的文件类型"),
    DOC_SIZE_EXCEEDED(3004, "文件大小超出限制"),
    DOC_DUPLICATE(3005, "文档已存在（相同内容）"),

    // AI 错误码 4xxx
    AI_SERVICE_ERROR(4001, "AI 服务调用失败"),
    AI_RATE_LIMIT(4002, "AI 接口调用频率超限");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
