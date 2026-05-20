package com.smartdocs.aikb.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "注册请求体")
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,32}$", message = "用户名只能包含字母数字_-，长度 3~32")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在 6~64 个字符之间")
    private String password;

    @Size(max = 64)
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128)
    private String email;
}
