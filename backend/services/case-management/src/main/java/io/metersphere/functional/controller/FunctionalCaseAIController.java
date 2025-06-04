package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalCaseAIConfigDTO;
import io.metersphere.functional.service.FunctionalCaseAIService;
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

}
