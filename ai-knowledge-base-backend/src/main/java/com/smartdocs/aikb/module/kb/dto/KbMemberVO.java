package com.smartdocs.aikb.module.kb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "知识库成员视图对象")
public class KbMemberVO {

    private Long id;

    private Long kbId;

    private Long userId;

    private String username;

    private String nickname;

    private String role;
}
