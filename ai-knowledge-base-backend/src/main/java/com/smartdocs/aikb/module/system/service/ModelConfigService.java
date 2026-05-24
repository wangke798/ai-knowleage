package com.smartdocs.aikb.module.system.service;

import com.smartdocs.aikb.module.system.entity.SysModelConfig;

import java.util.List;
import java.util.Map;

/**
 * 模型配置管理服务。
 * <p>主要供管理员查看/编辑 LLM / Embedding 接入参数，
 * 前端在"设置 → 模型配置"页面使用。
 */
public interface ModelConfigService {

    /** 查询所有（不含软删除）。 */
    List<Map<String, Object>> listAll();

    /** 查询指定类型（CHAT / EMBEDDING）。 */
    List<Map<String, Object>> listByType(String modelType);

    /** 保存或更新一条配置（id 为 null 则新增）。 */
    Map<String, Object> save(Map<String, Object> params);

    /** 切换启用/禁用状态，返回最新 enabled 值。 */
    int toggleEnabled(Long id);

    /** 设置某条配置为同类型的默认配置（原默认取消）。 */
    void setDefault(Long id);

    /** 删除一条配置。 */
    void delete(Long id);
}
