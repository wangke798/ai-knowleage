package com.smartdocs.aikb.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.module.system.entity.SysModelConfig;
import com.smartdocs.aikb.module.system.mapper.SysModelConfigMapper;
import com.smartdocs.aikb.module.system.service.ModelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl implements ModelConfigService {

    private final SysModelConfigMapper mapper;

    @Override
    public List<Map<String, Object>> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<SysModelConfig>()
                        .orderByAsc(SysModelConfig::getModelType)
                        .orderByDesc(SysModelConfig::getIsDefault))
                .stream().map(this::toMap).toList();
    }

    @Override
    public List<Map<String, Object>> listByType(String modelType) {
        return mapper.selectList(new LambdaQueryWrapper<SysModelConfig>()
                        .eq(SysModelConfig::getModelType, modelType)
                        .orderByDesc(SysModelConfig::getIsDefault))
                .stream().map(this::toMap).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> save(Map<String, Object> params) {
        String modelType = (String) params.get("modelType");
        String provider  = (String) params.get("provider");
        String modelName = (String) params.get("modelName");
        if (!StringUtils.hasText(modelType) || !StringUtils.hasText(provider) || !StringUtils.hasText(modelName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "modelType / provider / modelName 不能为空");
        }

        Long id = toLong(params.get("id"));
        SysModelConfig entity = (id != null) ? mapper.selectById(id) : null;
        if (entity == null) {
            entity = new SysModelConfig();
        }
        entity.setModelType(modelType.toUpperCase());
        entity.setProvider(provider.toUpperCase());
        entity.setModelName(modelName);
        entity.setBaseUrl((String) params.get("baseUrl"));
        entity.setDescription((String) params.get("description"));
        // API Key 为空时保留原值（不覆盖）
        String apiKey = (String) params.get("apiKey");
        if (StringUtils.hasText(apiKey)) {
            entity.setApiKey(apiKey);
        }
        if (params.containsKey("enabled")) {
            entity.setEnabled(toInt(params.get("enabled"), 1));
        } else {
            entity.setEnabled(1);
        }
        if (entity.getId() == null) {
            entity.setIsDefault(0);
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return toMap(entity);
    }

    @Override
    @Transactional
    public int toggleEnabled(Long id) {
        SysModelConfig entity = requireById(id);
        int newVal = (entity.getEnabled() != null && entity.getEnabled() == 1) ? 0 : 1;
        mapper.update(null, new LambdaUpdateWrapper<SysModelConfig>()
                .eq(SysModelConfig::getId, id)
                .set(SysModelConfig::getEnabled, newVal));
        return newVal;
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        SysModelConfig entity = requireById(id);
        // 取消同类型原默认
        mapper.update(null, new LambdaUpdateWrapper<SysModelConfig>()
                .eq(SysModelConfig::getModelType, entity.getModelType())
                .set(SysModelConfig::getIsDefault, 0));
        // 设置新默认
        mapper.update(null, new LambdaUpdateWrapper<SysModelConfig>()
                .eq(SysModelConfig::getId, id)
                .set(SysModelConfig::getIsDefault, 1));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireById(id);
        mapper.deleteById(id);
    }

    // ─── helpers ───

    private SysModelConfig requireById(Long id) {
        SysModelConfig entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "模型配置不存在");
        }
        return entity;
    }

    private Map<String, Object> toMap(SysModelConfig e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("modelType", e.getModelType());
        m.put("provider", e.getProvider());
        m.put("modelName", e.getModelName());
        // API Key 脱敏显示（仅展示后 4 位）
        String ak = e.getApiKey();
        m.put("apiKeyMasked", (ak != null && ak.length() > 4)
                ? "****" + ak.substring(ak.length() - 4) : (ak != null ? "****" : null));
        m.put("baseUrl", e.getBaseUrl());
        m.put("isDefault", e.getIsDefault() != null && e.getIsDefault() == 1);
        m.put("enabled", e.getEnabled() != null && e.getEnabled() == 1);
        m.put("description", e.getDescription());
        m.put("createTime", e.getCreateTime());
        m.put("updateTime", e.getUpdateTime());
        return m;
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static int toInt(Object v, int def) {
        if (v == null) return def;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return def; }
    }
}
