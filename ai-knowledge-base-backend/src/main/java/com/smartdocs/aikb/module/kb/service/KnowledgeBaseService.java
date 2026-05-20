package com.smartdocs.aikb.module.kb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartdocs.aikb.common.result.PageVO;
import com.smartdocs.aikb.module.kb.dto.KbUpsertRequest;
import com.smartdocs.aikb.module.kb.dto.KbVO;

public interface KnowledgeBaseService {

    /** 创建知识库（创建者自动成为 OWNER）。 */
    KbVO create(Long userId, KbUpsertRequest request);

    /** 更新知识库基础信息。仅 OWNER/EDITOR 可操作。 */
    KbVO update(Long userId, Long kbId, KbUpsertRequest request);

    /** 删除知识库。仅 OWNER 可操作。 */
    void delete(Long userId, Long kbId);

    /** 获取知识库详情，附带当前用户角色。 */
    KbVO detail(Long userId, Long kbId);

    /**
     * 分页查询当前用户可见的知识库（自己拥有的 + 作为成员加入的）。
     *
     * @param keyword 名称模糊匹配，可空
     */
    PageVO<KbVO> page(Long userId, long page, long size, String keyword);

    /** 校验用户在指定 KB 的角色权限。返回角色字符串，无权限时返回 {@code null}。 */
    String resolveRole(Long userId, Long kbId);
}
