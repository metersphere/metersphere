package io.metersphere.api.controller.scenario;

import io.metersphere.api.dto.scenario.ApiScenarioSelectAssociateDTO;
import io.metersphere.api.dto.scenario.ApiScenarioStepDTO;
import io.metersphere.api.service.scenario.ApiScenarioSelectAssociateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/scenario/associate")
@Tag(name = "接口测试-接口场景管理-场景导入系统参数")
public class ApiScenarioSelectAssociateController {

    @Resource
    private ApiScenarioSelectAssociateService apiScenarioSelectAssociateService;


    @PostMapping("/all")
    @Operation(summary = "接口场景管理-场景导入系统参数")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    public List<ApiScenarioStepDTO> getSelectDto(@Validated @RequestBody Map<String, ApiScenarioSelectAssociateDTO> requestMap) {
        return apiScenarioSelectAssociateService.getSelectDto(requestMap);
    }
}
