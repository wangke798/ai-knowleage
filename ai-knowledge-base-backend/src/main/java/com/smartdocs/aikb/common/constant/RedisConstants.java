package com.smartdocs.aikb.common.constant;

public interface RedisConstants {

    // Token 黑名单 key 前缀，value: "1"，TTL = access token 剩余有效期
    String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    // Refresh Token key 前缀，value: userId，TTL = refresh token 有效期
    String REFRESH_TOKEN_PREFIX = "token:refresh:";

    // 用户信息缓存 key 前缀
    String USER_INFO_PREFIX = "user:info:";

    // 知识库信息缓存 key 前缀
    String KB_INFO_PREFIX = "kb:info:";

    // RAG 问答缓存 key 前缀（相同问题+KB 命中缓存）
    String CHAT_CACHE_PREFIX = "chat:cache:";

    // 限流 key 前缀（按用户+接口）
    String RATE_LIMIT_PREFIX = "rate:limit:";
}
