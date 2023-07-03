package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ListRequestDTO;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.validation.groups.Created;
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

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_ADD_API)
    @Log(type = OperationLogType.ADD, module = OperationLogModule.API_DEFINITION, projectId = "#request.projectId", details = "#request.name")
    public ApiDefinitionDTO add(@Validated({Created.class}) @RequestBody ApiDefinitionDTO request,
                                @RequestParam(value = "files") List<MultipartFile> bodyFiles) {
        return apiDefinitionService.create(request, bodyFiles);
    }

    @PostMapping(value = "/batch-del")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ_DELETE)
    @Log(isBatch = true, isBefore = true, details = "#msClass.getLogs(#ids)", msClass = ApiDefinitionService.class)
    public void batchDelete(@RequestBody List<String> ids) {
        apiDefinitionService.batchDelete(ids);
    }

    @PostMapping(value = "/page")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public Pager<List<ApiDefinition>> list(@Validated @RequestBody ListRequestDTO request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getOrderColumn()) ? request.getOrderColumn() : "create_time desc");
        return PageUtils.setPageInfo(page, apiDefinitionService.list(request));
    }

}
