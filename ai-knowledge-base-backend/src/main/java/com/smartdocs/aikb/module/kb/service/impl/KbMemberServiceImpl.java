package com.smartdocs.aikb.module.kb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.module.kb.dto.KbMemberRequest;
import com.smartdocs.aikb.module.kb.dto.KbMemberVO;
import com.smartdocs.aikb.module.kb.entity.KbMember;
import com.smartdocs.aikb.module.kb.mapper.KbMemberMapper;
import com.smartdocs.aikb.module.kb.service.KbMemberService;
import com.smartdocs.aikb.module.kb.service.KnowledgeBaseService;
import com.smartdocs.aikb.module.user.entity.SysUser;
import com.smartdocs.aikb.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.smartdocs.aikb.module.kb.service.impl.KnowledgeBaseServiceImpl.ROLE_OWNER;

@Service
@RequiredArgsConstructor
public class KbMemberServiceImpl implements KbMemberService {

    private final KbMemberMapper kbMemberMapper;
    private final SysUserMapper sysUserMapper;
    private final KnowledgeBaseService knowledgeBaseService;

    private static final Set<String> ALLOWED_ROLES = Set.of("OWNER", "EDITOR", "VIEWER");

    @Override
    public List<KbMemberVO> list(Long currentUserId, Long kbId) {
        // 成员（含 OWNER）可查看；非成员拒绝
        if (knowledgeBaseService.resolveRole(currentUserId, kbId) == null) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION);
        }
        List<KbMember> members = kbMemberMapper.selectList(new LambdaQueryWrapper<KbMember>()
                .eq(KbMember::getKbId, kbId));
        if (members.isEmpty()) {
            return List.of();
        }
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(
                        members.stream().map(KbMember::getUserId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        return members.stream().map(m -> toVO(m, userMap.get(m.getUserId()))).toList();
    }

    @Override
    @Transactional
    public KbMemberVO add(Long currentUserId, Long kbId, KbMemberRequest request) {
        requireOwner(currentUserId, kbId);
        validateRole(request.getRole());

        SysUser user = sysUserMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Long exists = kbMemberMapper.selectCount(new LambdaQueryWrapper<KbMember>()
                .eq(KbMember::getKbId, kbId)
                .eq(KbMember::getUserId, request.getUserId()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.KB_MEMBER_EXISTS);
        }

        KbMember member = new KbMember();
        member.setKbId(kbId);
        member.setUserId(request.getUserId());
        member.setRole(request.getRole());
        kbMemberMapper.insert(member);

        return toVO(member, user);
    }

    @Override
    @Transactional
    public KbMemberVO updateRole(Long currentUserId, Long kbId, Long memberId, String role) {
        requireOwner(currentUserId, kbId);
        validateRole(role);

        KbMember member = requireMember(kbId, memberId);
        if (ROLE_OWNER.equals(member.getRole())) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION, "不能修改所有者的角色");
        }
        if (ROLE_OWNER.equals(role)) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION, "不能通过此接口转让所有权");
        }

        member.setRole(role);
        kbMemberMapper.updateById(member);
        return toVO(member, sysUserMapper.selectById(member.getUserId()));
    }

    @Override
    @Transactional
    public void remove(Long currentUserId, Long kbId, Long memberId) {
        requireOwner(currentUserId, kbId);
        KbMember member = requireMember(kbId, memberId);
        if (ROLE_OWNER.equals(member.getRole())) {
            throw new BusinessException(ResultCode.KB_OWNER_CANNOT_REMOVE);
        }
        kbMemberMapper.deleteById(memberId);
    }

    // ------------------------- helpers -------------------------

    private void requireOwner(Long userId, Long kbId) {
        String role = knowledgeBaseService.resolveRole(userId, kbId);
        if (!ROLE_OWNER.equals(role)) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION, "仅所有者可管理成员");
        }
    }

    private KbMember requireMember(Long kbId, Long memberId) {
        KbMember m = kbMemberMapper.selectById(memberId);
        if (m == null || !m.getKbId().equals(kbId)) {
            throw new BusinessException(ResultCode.KB_MEMBER_NOT_FOUND);
        }
        return m;
    }

    private void validateRole(String role) {
        if (!ALLOWED_ROLES.contains(role)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法的角色：" + role);
        }
    }

    private KbMemberVO toVO(KbMember member, SysUser user) {
        KbMemberVO vo = new KbMemberVO();
        vo.setId(member.getId());
        vo.setKbId(member.getKbId());
        vo.setUserId(member.getUserId());
        vo.setRole(member.getRole());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        }
        return vo;
    }
}
