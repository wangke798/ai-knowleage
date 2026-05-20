package com.smartdocs.aikb.module.kb.service;

import com.smartdocs.aikb.module.kb.dto.KbMemberRequest;
import com.smartdocs.aikb.module.kb.dto.KbMemberVO;

import java.util.List;

public interface KbMemberService {

    /** 列出指定知识库的所有成员。仅成员可查看。 */
    List<KbMemberVO> list(Long currentUserId, Long kbId);

    /** 添加成员。仅 OWNER 可操作。 */
    KbMemberVO add(Long currentUserId, Long kbId, KbMemberRequest request);

    /** 修改成员角色。仅 OWNER 可操作；不能修改 OWNER 本人。 */
    KbMemberVO updateRole(Long currentUserId, Long kbId, Long memberId, String role);

    /** 移除成员。仅 OWNER 可操作；不能移除 OWNER 本人。 */
    void remove(Long currentUserId, Long kbId, Long memberId);
}
