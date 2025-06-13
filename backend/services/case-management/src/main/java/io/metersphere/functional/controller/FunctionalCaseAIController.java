package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalCaseAIConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAiDTO;
import io.metersphere.functional.request.FunctionalCaseAIChatRequest;
import io.metersphere.functional.service.FunctionalCaseAIService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author guoyuqi
 */
@Tag(name = "用例管理-功能用例-AI生成用例")
@RestController
@RequestMapping("/functional/case/ai")
public class FunctionalCaseAIController {

    @Resource
    private FunctionalCaseAIService functionalCaseAIService;

    @GetMapping("/get/config")
    @Operation(summary = "用例管理-功能用例-获取用户AI提示词")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public FunctionalCaseAIConfigDTO getUserPrompt() {
        return functionalCaseAIService.getUserPrompt(SessionUtils.getUserId());
    }

    @PostMapping("/save/config")
    @Operation(summary = "用例管理-功能用例-保存用户AI提示词")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    public void saveUserPrompt(@RequestBody FunctionalCaseAIConfigDTO configDTO) {
        functionalCaseAIService.saveUserPrompt(SessionUtils.getUserId(), configDTO);
    }

    @PostMapping("/transform")
    @Operation(summary = "用例管理-功能用例-单条AI数据生成用例对象")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public FunctionalCaseAiDTO transformToDTO(@Validated @RequestBody AIChatRequest request) {
        return functionalCaseAIService.transformToDTO(request);
    }

    @PostMapping(value = "/chat")
    @Operation(summary = "用例管理-功能用例-AI对话")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public String chat(@RequestBody AIChatRequest request) {
        return functionalCaseAIService.chat(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch/save")
    @Operation(summary = "用例管理-功能用例-多条AI数据生成用例对象")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void transformToDTO(@Validated @RequestBody FunctionalCaseAIChatRequest request) {
         functionalCaseAIService.batchSave(request, SessionUtils.getUserId());
    }

}
