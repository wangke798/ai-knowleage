package com.smartdocs.aikb.module.kb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("kb_member")
public class KbMember {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long kbId;

    private Long userId;

    /** 角色：OWNER / EDITOR / VIEWER */
    private String role;
}
