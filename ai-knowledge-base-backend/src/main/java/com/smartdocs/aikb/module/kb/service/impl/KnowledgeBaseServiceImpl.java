package com.smartdocs.aikb.module.kb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.PageVO;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.module.kb.dto.KbUpsertRequest;
import com.smartdocs.aikb.module.kb.dto.KbVO;
import com.smartdocs.aikb.module.kb.entity.KbMember;
import com.smartdocs.aikb.module.kb.entity.KnowledgeBase;
import com.smartdocs.aikb.module.kb.mapper.KbMemberMapper;
import com.smartdocs.aikb.module.kb.mapper.KnowledgeBaseMapper;
import com.smartdocs.aikb.module.kb.service.KnowledgeBaseService;
import com.smartdocs.aikb.module.user.entity.SysUser;
import com.smartdocs.aikb.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    /** 角色常量：所有者 / 编辑者 / 只读者。 */
    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_EDITOR = "EDITOR";
    public static final String ROLE_VIEWER = "VIEWER";

    private static final Set<String> WRITABLE_ROLES = Set.of(ROLE_OWNER, ROLE_EDITOR);

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KbMemberMapper kbMemberMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional
    public KbVO create(Long userId, KbUpsertRequest request) {
        // 同一用户下名称去重
        Long duplicate = knowledgeBaseMapper.selectCount(new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getOwnerId, userId)
                .eq(KnowledgeBase::getName, request.getName()));
        if (duplicate != null && duplicate > 0) {
            throw new BusinessException(ResultCode.KB_NAME_DUPLICATE);
        }

        KnowledgeBase entity = new KnowledgeBase();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setEmbeddingModel(request.getEmbeddingModel());
        entity.setOwnerId(userId);
        entity.setStatus(1);
        knowledgeBaseMapper.insert(entity);

        // 创建者自动作为 OWNER 成员
        KbMember owner = new KbMember();
        owner.setKbId(entity.getId());
        owner.setUserId(userId);
        owner.setRole(ROLE_OWNER);
        kbMemberMapper.insert(owner);

        return toVO(entity, ROLE_OWNER, userId);
    }

    @Override
    @Transactional
    public KbVO update(Long userId, Long kbId, KbUpsertRequest request) {
        KnowledgeBase entity = requireKb(kbId);
        String role = resolveRole(userId, kbId);
        if (role == null || !WRITABLE_ROLES.contains(role)) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION);
        }

        // 同名校验（排除自身）
        Long duplicate = knowledgeBaseMapper.selectCount(new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getOwnerId, entity.getOwnerId())
                .eq(KnowledgeBase::getName, request.getName())
                .ne(KnowledgeBase::getId, kbId));
        if (duplicate != null && duplicate > 0) {
            throw new BusinessException(ResultCode.KB_NAME_DUPLICATE);
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getEmbeddingModel())) {
            entity.setEmbeddingModel(request.getEmbeddingModel());
        }
        knowledgeBaseMapper.updateById(entity);

        return toVO(entity, role, entity.getOwnerId());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long kbId) {
        KnowledgeBase entity = requireKb(kbId);
        if (!entity.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION, "仅所有者可删除知识库");
        }
        // 逻辑删除 KB
        knowledgeBaseMapper.deleteById(kbId);
        // 物理删除成员关系（kb_member 无 deleted 字段）
        kbMemberMapper.delete(new LambdaQueryWrapper<KbMember>().eq(KbMember::getKbId, kbId));
    }

    @Override
    public KbVO detail(Long userId, Long kbId) {
        KnowledgeBase entity = requireKb(kbId);
        String role = resolveRole(userId, kbId);
        if (role == null) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION);
        }
        return toVO(entity, role, entity.getOwnerId());
    }

    @Override
    public PageVO<KbVO> page(Long userId, long page, long size, String keyword) {
        // 1. 找出该用户作为成员加入的所有 kbId
        List<Long> joinedKbIds = kbMemberMapper.selectList(new LambdaQueryWrapper<KbMember>()
                        .select(KbMember::getKbId)
                        .eq(KbMember::getUserId, userId))
                .stream().map(KbMember::getKbId).distinct().toList();

        LambdaQueryWrapper<KnowledgeBase> qw = new LambdaQueryWrapper<>();
        if (joinedKbIds.isEmpty()) {
            qw.eq(KnowledgeBase::getOwnerId, userId);
        } else {
            qw.and(w -> w.eq(KnowledgeBase::getOwnerId, userId)
                    .or().in(KnowledgeBase::getId, joinedKbIds));
        }
        if (StringUtils.hasText(keyword)) {
            qw.like(KnowledgeBase::getName, keyword);
        }
        qw.orderByDesc(KnowledgeBase::getUpdateTime);

        Page<KnowledgeBase> p = new Page<>(page, size);
        Page<KnowledgeBase> result = knowledgeBaseMapper.selectPage(p, qw);

        List<KnowledgeBase> records = result.getRecords();
        if (records.isEmpty()) {
            return PageVO.map(result, kb -> toVO(kb, null, kb.getOwnerId()));
        }

        // 一次查出本页所有 KB 中 当前用户 的角色
        List<Long> pageKbIds = records.stream().map(KnowledgeBase::getId).toList();
        Map<Long, String> roleMap = kbMemberMapper.selectList(new LambdaQueryWrapper<KbMember>()
                        .eq(KbMember::getUserId, userId)
                        .in(KbMember::getKbId, pageKbIds))
                .stream().collect(Collectors.toMap(KbMember::getKbId, KbMember::getRole, (a, b) -> a));

        // 批量查 owner 信息（用于展示 ownerName）
        Set<Long> ownerIds = records.stream().map(KnowledgeBase::getOwnerId).collect(Collectors.toSet());
        Map<Long, String> ownerNameMap = loadOwnerNames(ownerIds);

        return PageVO.map(result, kb -> {
            String role = roleMap.get(kb.getId());
            // 若是 owner 但没有 member 记录（兼容历史数据），按 OWNER 处理
            if (role == null && kb.getOwnerId().equals(userId)) {
                role = ROLE_OWNER;
            }
            KbVO vo = toVO(kb, role, kb.getOwnerId());
            vo.setOwnerName(ownerNameMap.get(kb.getOwnerId()));
            return vo;
        });
    }

    @Override
    public String resolveRole(Long userId, Long kbId) {
        KbMember member = kbMemberMapper.selectOne(new LambdaQueryWrapper<KbMember>()
                .eq(KbMember::getKbId, kbId)
                .eq(KbMember::getUserId, userId)
                .last("LIMIT 1"));
        if (member != null) {
            return member.getRole();
        }
        // 兜底：owner 但没有 member 记录
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb != null && kb.getOwnerId().equals(userId)) {
            return ROLE_OWNER;
        }
        return null;
    }

    // ------------------------- helpers -------------------------

    private KnowledgeBase requireKb(Long kbId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null) {
            throw new BusinessException(ResultCode.KB_NOT_FOUND);
        }
        return kb;
    }

    private KbVO toVO(KnowledgeBase entity, String role, Long ownerId) {
        KbVO vo = new KbVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setCurrentUserRole(role);
        if (ownerId != null && vo.getOwnerName() == null) {
            // 单条详情场景按需查询，避免 N+1
            SysUser u = sysUserMapper.selectById(ownerId);
            if (u != null) {
                vo.setOwnerName(StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername());
            }
        }
        return vo;
    }

    private Map<Long, String> loadOwnerNames(Collection<Long> ownerIds) {
        if (ownerIds == null || ownerIds.isEmpty()) {
            return Map.of();
        }
        List<SysUser> users = sysUserMapper.selectBatchIds(ownerIds);
        return users.stream().collect(Collectors.toMap(
                SysUser::getId,
                u -> StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername(),
                (a, b) -> a));
    }
}
