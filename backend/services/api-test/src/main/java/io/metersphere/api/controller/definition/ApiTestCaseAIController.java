package io.metersphere.api.controller.definition;

import io.metersphere.api.dto.ApiCaseAIConfigDTO;
import io.metersphere.api.dto.ApiCaseAiResponse;
import io.metersphere.api.dto.definition.ApiCaseAiTransformDTO;
import io.metersphere.api.dto.definition.ApiTestCaseAIRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.service.definition.ApiTestCaseAIService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/case/ai")
@Tag(name = "接口测试-接口管理-接口用例-AI生成")
public class ApiTestCaseAIController {
    @Resource
    private ApiTestCaseAIService apiTestCaseAIService;

    @PostMapping(value = "/chat")
    @Operation(summary = "聊天")
    @CheckOwner(resourceId = "#request.apiDefinitionId", resourceType = "api_definition")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public String chat(@Validated @RequestBody ApiTestCaseAIRequest request) {
        return apiTestCaseAIService.chat(request, SessionUtils.getUserId());
    }

    @GetMapping("/get/config")
    @Operation(summary = "接口管理-接口用例-获取用户AI提示词配置")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public ApiCaseAIConfigDTO getUserPrompt() {
        return apiTestCaseAIService.getUserPrompt(SessionUtils.getUserId());
    }

    @PostMapping("/save/config")
    @Operation(summary = "接口管理-接口用例-保存用户AI提示词配置")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    public void saveUserPrompt(@RequestBody ApiCaseAIConfigDTO promptDTO) {
        apiTestCaseAIService.saveUserPrompt(SessionUtils.getUserId(), promptDTO);
    }


    @PostMapping("/transform")
    @Operation(summary = "接口管理-接口用例-单条AI数据生成用例对象")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public ApiTestCaseDTO transformToDTO(@Validated @RequestBody ApiCaseAiTransformDTO request) {
        return apiTestCaseAIService.transformToDTO(request);
    }

    @PostMapping("/batch/save")
    @Operation(summary = "接口管理-接口用例-批量保存AI用例")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD)
    public ApiCaseAiResponse batchSave(@Validated @RequestBody ApiCaseAiTransformDTO request) {
        return apiTestCaseAIService.batchSave(request, SessionUtils.getUserId());
    }

}
