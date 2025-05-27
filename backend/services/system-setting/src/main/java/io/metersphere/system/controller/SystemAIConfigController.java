package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.request.ai.ModelSourceDTO;
import io.metersphere.system.dto.request.ai.ModelSourceRequest;
import io.metersphere.system.service.SystemAIConfigService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/config")
@Tag(name = "系统设置-组织-服务集成")
public class SystemAIConfigController {

    @Resource
    private SystemAIConfigService systemAIConfigService;

    @PostMapping("/edit-source")
    @Operation(summary = "系统设置-编辑模型设置")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AI_MODEL_UPDATE)
    public void editModuleConfig(@Validated @RequestBody ModelSourceDTO modelSourceDTO) {
        systemAIConfigService.editModuleConfig(modelSourceDTO, SessionUtils.getUserId());
    }

    @PostMapping("/source/list")
    @Operation(summary = "系统设置-查看模型集合")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AI_MODEL_READ)
    public Pager<List<ModelSourceDTO>> getModelSourceList(@Validated @RequestBody ModelSourceRequest modelSourceRequest) {
        Page<Object> page = PageHelper.startPage(modelSourceRequest.getCurrent(), modelSourceRequest.getPageSize());
        return PageUtils.setPageInfo(page, systemAIConfigService.getModelSourceList(modelSourceRequest));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模型信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AI_MODEL_READ)
    public ModelSourceDTO getModelInformation(@PathVariable String id) {
        return systemAIConfigService.getModelSourceDTO(id);
    }

}
