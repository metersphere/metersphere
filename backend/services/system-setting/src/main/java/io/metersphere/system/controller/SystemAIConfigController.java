package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.dto.request.ai.AiModelSourceRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
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
@Tag(name = "系统设置-AI-模型配置")
public class SystemAIConfigController {

    @Resource
    private SystemAIConfigService systemAIConfigService;

    @PostMapping("/edit-source")
    @Operation(summary = "系统设置-编辑模型设置")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AI_MODEL_UPDATE)
    public void editModuleConfig(@Validated @RequestBody AiModelSourceDTO aiModelSourceDTO) {
        systemAIConfigService.editModuleConfig(aiModelSourceDTO, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除模型")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AI_MODEL_UPDATE)
    public void delModelInformation(@PathVariable String id) {
        systemAIConfigService.delModelInformation(id, null);
    }

    @PostMapping("/source/list")
    @Operation(summary = "系统设置-查看模型集合")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AI_MODEL_READ)
    public Pager<List<AiModelSourceDTO>> getModelSourceList(@Validated @RequestBody AiModelSourceRequest aiModelSourceRequest) {
        Page<Object> page = PageHelper.startPage(aiModelSourceRequest.getCurrent(), aiModelSourceRequest.getPageSize());
        return PageUtils.setPageInfo(page, systemAIConfigService.getModelSourceList(aiModelSourceRequest));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模型信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AI_MODEL_READ)
    public AiModelSourceDTO getModelInformation(@PathVariable String id) {
        return systemAIConfigService.getModelSourceDTO(id, null);
    }

    @GetMapping("/source/name/list")
    @Operation(summary = "系统设置-查看模型名称集合")
    public List<OptionDTO> getModelSourceNameList() {
        return systemAIConfigService.getModelSourceNameList(SessionUtils.getUserId());
    }

}
