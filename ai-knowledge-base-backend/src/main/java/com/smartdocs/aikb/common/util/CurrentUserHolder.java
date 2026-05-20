package com.smartdocs.aikb.common.util;

import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户上下文工具。
 * <p>认证主体由 {@code JwtAuthenticationFilter} 写入 SecurityContext，
 * principal 为 {@link AuthUser}。
 */
public final class CurrentUserHolder {

    private CurrentUserHolder() {
    }

    /** 获取当前登录用户 ID。未登录时抛 401。 */
    public static Long requireUserId() {
        AuthUser user = currentAuthUser();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user.getUserId();
    }

    /** 获取当前登录用户名（可能为空）。 */
    public static String currentUsername() {
        AuthUser user = currentAuthUser();
        return user == null ? null : user.getUsername();
    }

    public static AuthUser currentAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return principal instanceof AuthUser ? (AuthUser) principal : null;
    }
}
