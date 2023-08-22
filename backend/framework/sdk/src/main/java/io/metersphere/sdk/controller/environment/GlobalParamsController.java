package io.metersphere.sdk.controller.environment;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.environment.GlobalParamsRequest;
import io.metersphere.sdk.service.environment.GlobalParamsService;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project/global/params")
@Tag(name = "项目管理-环境-全局参数")
public class GlobalParamsController {

    @Resource
    private GlobalParamsService globalParamsService;

    @PostMapping("/add")
    @Operation(summary = "项目管理-环境-全局参数-新增")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD)
    public GlobalParamsRequest add(@Validated({Created.class}) @RequestBody GlobalParamsRequest globalParamsRequest) {
        return globalParamsService.add(globalParamsRequest, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-环境-全局参数-修改")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE)
    public GlobalParamsRequest update(@Validated({Updated.class}) @RequestBody GlobalParamsRequest globalParamsRequest) {
        return globalParamsService.update(globalParamsRequest, SessionUtils.getUserId());
    }

    @GetMapping("/get/{projectId}")
    @Operation(summary = "项目管理-环境-全局参数-详情")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    public GlobalParamsRequest get(@PathVariable String projectId) {
        return globalParamsService.get(projectId);
    }


}
