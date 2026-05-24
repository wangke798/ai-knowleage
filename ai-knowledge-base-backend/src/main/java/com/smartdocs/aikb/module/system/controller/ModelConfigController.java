package com.smartdocs.aikb.module.system.controller;

import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.module.system.service.ModelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模型配置管理（管理员接口）。
 *
 * <pre>
 *   GET    /system/model-configs          — 查询所有
 *   GET    /system/model-configs?type=CHAT — 按类型查询
 *   POST   /system/model-configs          — 新增 / 更新
 *   POST   /system/model-configs/{id}/toggle-enabled — 切换启用
 *   POST   /system/model-configs/{id}/set-default    — 设为默认
 *   DELETE /system/model-configs/{id}    — 删除
 * </pre>
 */
@RestController
@RequestMapping("/system/model-configs")
@RequiredArgsConstructor
public class ModelConfigController {

    private final ModelConfigService modelConfigService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String type) {
        if (type != null && !type.isBlank()) {
            return Result.success(modelConfigService.listByType(type.toUpperCase()));
        }
        return Result.success(modelConfigService.listAll());
    }

    @PostMapping
    public Result<Map<String, Object>> save(@RequestBody Map<String, Object> params) {
        return Result.success(modelConfigService.save(params));
    }

    @PostMapping("/{id}/toggle-enabled")
    public Result<Map<String, Object>> toggleEnabled(@PathVariable Long id) {
        int enabled = modelConfigService.toggleEnabled(id);
        return Result.success(Map.of("enabled", enabled == 1));
    }

    @PostMapping("/{id}/set-default")
    public Result<Void> setDefault(@PathVariable Long id) {
        modelConfigService.setDefault(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        modelConfigService.delete(id);
        return Result.success();
    }
}
