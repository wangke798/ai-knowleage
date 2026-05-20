package com.smartdocs.aikb.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Security 上下文中的当前认证主体。
 * 经过 JwtAuthenticationFilter 后会作为 {@code Authentication#getPrincipal()} 注入。
 */
@Getter
public class AuthUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final List<String> roles;

    public AuthUser(Long userId, String username, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.roles = roles == null ? List.of() : roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    @Override public String getPassword() { return null; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
