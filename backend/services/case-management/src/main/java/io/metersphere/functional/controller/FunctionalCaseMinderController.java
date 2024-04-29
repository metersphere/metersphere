package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalMinderTreeDTO;
import io.metersphere.functional.request.FunctionalCaseMindRequest;
import io.metersphere.functional.service.FunctionalCaseMinderService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
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

/**
 * @author guoyuqi
 */
@Tag(name = "用例管理-功能用例-脑图")
@RestController
@RequestMapping("/functional/mind/case")
public class FunctionalCaseMinderController {

    @Resource
    private FunctionalCaseMinderService functionalCaseMinderService;

    @PostMapping("/list")
    @Operation(summary = "用例管理-功能用例-脑图用例跟根据模块ID查询列表")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_MINDER)
    @CheckOwner(resourceId = "#projectId()", resourceType = "project")
    public List<FunctionalMinderTreeDTO> getFunctionalCaseMinderTree(@Validated @RequestBody FunctionalCaseMindRequest request) {
        return functionalCaseMinderService.getMindFunctionalCase(request, false);
    }

}
