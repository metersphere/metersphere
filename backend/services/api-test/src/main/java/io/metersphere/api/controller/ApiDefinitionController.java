package io.metersphere.api.controller;

import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.service.APIDefinitionLogService;
import io.metersphere.api.service.APIDefinitionService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping(value = "/api/definition")
public class ApiDefinitionController {
    @Resource
    private APIDefinitionService apiDefinitionService;

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_ADD_API)
    // 添加接口Log示例
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = APIDefinitionLogService.class)
    public ApiDefinitionDTO add(@Validated({Created.class}) @RequestBody ApiDefinitionDTO request,
                                @RequestParam(value = "files") List<MultipartFile> bodyFiles) {
        return apiDefinitionService.create(request, bodyFiles);
    }

    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_ADD_API)
    // 添加修改Log示例
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = APIDefinitionLogService.class)
    public ApiDefinitionDTO update(@Validated({Updated.class}) @RequestBody ApiDefinitionDTO request) {
        return request;
    }

    @PostMapping(value = "/batch-update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_ADD_API)
    // 添加修改Log示例
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchUpdateLog(#ids)", msClass = APIDefinitionLogService.class)
    public ApiDefinitionDTO batchUpdate(@RequestBody List<String> ids) {
        return null;
    }

    @PostMapping(value = "/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.delLog(#id)", msClass = APIDefinitionLogService.class)
    public void batchDelete(@RequestBody String id) {

    }
    @PostMapping(value = "/batch-del")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.batchDelLog(#ids)", msClass = APIDefinitionLogService.class)
    public void batchDelete(@RequestBody List<String> ids) {
        apiDefinitionService.batchDelete(ids);
    }

}
