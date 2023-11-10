package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.request.ApiDefinitionPageRequest;
import io.metersphere.api.service.definition.ApiDefinitionLogService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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
    private ApiDefinitionService apiDefinitionService;

    @PostMapping(value = "/add")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_ADD_API)
    // 添加接口Log示例
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiDefinitionLogService.class)
    public ApiDefinitionDTO add(@Validated({Created.class}) @RequestPart("request") ApiDefinitionDTO request,
                                @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiDefinitionService.create(request, bodyFiles);
    }

    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_ADD_API)
    // 添加修改Log示例
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiDefinitionLogService.class)
    public ApiDefinitionDTO update(@Validated({Updated.class}) @RequestBody ApiDefinitionDTO request) {
        return request;
    }

    @PostMapping(value = "/batch-update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_ADD_API)
    // 添加修改Log示例
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchUpdateLog(#ids)", msClass = ApiDefinitionLogService.class)
    public ApiDefinitionDTO batchUpdate(@RequestBody List<String> ids) {
        return null;
    }

    @PostMapping(value = "/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.delLog(#id)", msClass = ApiDefinitionLogService.class)
    public void batchDelete(@RequestBody String id) {

    }
    @PostMapping(value = "/batch-del")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.batchDelLog(#ids)", msClass = ApiDefinitionLogService.class)
    public void batchDelete(@RequestBody List<String> ids) {
        apiDefinitionService.batchDelete(ids);
    }


    @PostMapping("/page")
    @Operation(summary = "接口测试-接口管理-接口列表")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public Pager<List<ApiDefinitionDTO>> getPage(@Validated @RequestBody ApiDefinitionPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiDefinitionService.getApiDefinitionPage(request, false));
    }

}
