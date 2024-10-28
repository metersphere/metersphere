package io.metersphere.api.controller.definition;

import io.metersphere.api.dto.definition.ApiScheduleDTO;
import io.metersphere.api.dto.definition.request.ApiScheduleRequest;
import io.metersphere.api.service.definition.ApiDefinitionScheduleService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/definition/schedule")
@Tag(name = "接口测试-接口管理-接口定义-定时同步")
public class ApiDefinitionScheduleController {
    @Resource
    private ApiDefinitionScheduleService apiDefinitionScheduleService;

    @PostMapping(value = "/add")
    @Operation(summary = "接口测试-接口管理-定时同步-创建")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_IMPORT)
    public String createSchedule(@RequestBody @Validated({Created.class}) ApiScheduleRequest request) {
        return apiDefinitionScheduleService.createSchedule(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/update")
    @Operation(summary = "接口测试-接口管理-定时同步-更新")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_IMPORT)
    @CheckOwner(resourceId = "#request.id", resourceType = "api_definition_swagger")
    public String updateSchedule(@RequestBody @Validated({Updated.class}) ApiScheduleRequest request) {
        return apiDefinitionScheduleService.updateSchedule(request, SessionUtils.getUserId());
    }

    @GetMapping(value = "/switch/{id}")
    @Operation(summary = "接口测试-接口管理-定时同步-开启/关闭")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_IMPORT)
    @CheckOwner(resourceId = "#id", resourceType = "schedule")
    public void updateScheduleEnable(@PathVariable String id) {
        apiDefinitionScheduleService.switchSchedule(id);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "接口测试-接口管理-定时同步-删除")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_IMPORT)
    @CheckOwner(resourceId = "#id", resourceType = "schedule")
    public void deleteSchedule(@PathVariable String id) {
        apiDefinitionScheduleService.deleteSchedule(id, SessionUtils.getUserId(), "/api/definition/schedule/delete/", HttpMethodConstants.GET.name(), OperationLogModule.API_TEST_MANAGEMENT_DEFINITION);
    }

    @GetMapping(value = "/get/{id}")
    @Operation(summary = "接口测试-接口管理-定时同步-查询")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_IMPORT)
    @CheckOwner(resourceId = "#id", resourceType = "api_definition_swagger")
    public ApiScheduleDTO getResourceId(@PathVariable String id) {
        return apiDefinitionScheduleService.getSchedule(id);
    }


}
