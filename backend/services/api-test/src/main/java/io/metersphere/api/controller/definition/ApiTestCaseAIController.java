package io.metersphere.api.controller.definition;

import io.metersphere.api.dto.definition.ApiTestCaseAIRequest;
import io.metersphere.api.service.definition.ApiTestCaseAIService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/case/ai")
@Tag(name = "接口测试-接口管理-接口用例-AI生成")
public class ApiTestCaseAIController {
    @Resource
    private ApiTestCaseAIService apiTestCaseAIService;

    @PostMapping(value = "/chat")
    @Operation(summary = "聊天")
    @CheckOwner(resourceId = "#request.id", resourceType = "api_definition")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public Object chat(@Validated @RequestBody ApiTestCaseAIRequest request) {
        return apiTestCaseAIService.chat(request, SessionUtils.getUserId());
    }
}
