package com.smartdocs.aikb.module.kb.service;

import java.util.List;
import java.util.Map;

public interface KbMemberService {

    List<Map<String, Object>> list(Long currentUserId, Long kbId);

    Map<String, Object> add(Long currentUserId, Long kbId, Map<String, Object> params);

    Map<String, Object> updateRole(Long currentUserId, Long kbId, Long memberId, String role);

    void remove(Long currentUserId, Long kbId, Long memberId);
}
