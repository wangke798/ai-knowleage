package com.smartdocs.aikb.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "当前登录用户信息")
public class UserInfoVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private List<String> roles;
}
