package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalCaseAIPromptDTO;
import io.metersphere.functional.service.FunctionalCaseAIPromptService;
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
@Tag(name = "用例管理-功能用例-AI提示词")
@RestController
@RequestMapping("/functional/case/ai/prompt")
public class FunctionalCaseAIPromptController {

    @Resource
    private FunctionalCaseAIPromptService functionalCaseAIPromptService;

    @GetMapping("/get")
    @Operation(summary = "用例管理-功能用例-获取用户AI提示词")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public FunctionalCaseAIPromptDTO getUserPrompt() {
        return functionalCaseAIPromptService.getUserPrompt(SessionUtils.getUserId());
    }

    @PostMapping("/save")
    @Operation(summary = "用例管理-功能用例-保存用户AI提示词")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    public void saveUserPrompt(@RequestBody FunctionalCaseAIPromptDTO promptDTO) {
        functionalCaseAIPromptService.saveUserPrompt(SessionUtils.getUserId(), promptDTO);
    }

}
