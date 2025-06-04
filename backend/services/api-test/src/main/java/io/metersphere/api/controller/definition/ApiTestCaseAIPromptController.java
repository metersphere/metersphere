package io.metersphere.api.controller.definition;

import io.metersphere.api.dto.ApiCaseAIPromptDTO;
import io.metersphere.api.service.definition.ApiTestCaseAIPromptService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

/**
 * @author guoyuqi
 */
@Tag(name = "接口管理-接口用例-AI提示词")
@RestController
@RequestMapping("/api/case/ai/prompt")
public class ApiTestCaseAIPromptController {

    @Resource
    private ApiTestCaseAIPromptService apiTestCaseAIPromptService;

    @GetMapping("/get")
    @Operation(summary = "接口管理-接口用例-获取用户AI提示词")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public ApiCaseAIPromptDTO getUserPrompt() {
        return apiTestCaseAIPromptService.getUserPrompt(SessionUtils.getUserId());
    }

    @PostMapping("/save")
    @Operation(summary = "接口管理-接口用例-保存用户AI提示词")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    public void saveUserPrompt(@RequestBody ApiCaseAIPromptDTO promptDTO) {
        apiTestCaseAIPromptService.saveUserPrompt(SessionUtils.getUserId(), promptDTO);
    }
}
