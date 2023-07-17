package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;

import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.log.vo.OperationLogRequest;
import io.metersphere.sdk.log.vo.OperationLogResponse;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.dto.OrganizationProjectOptionsDto;
import io.metersphere.system.dto.response.OrganizationProjectOptionsResponse;
import io.metersphere.system.service.OrganizationService;
import io.metersphere.system.service.SystemProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operation/log")
public class OperationLogController {

    @Resource
    private OrganizationService organizationService;

    @Resource
    private SystemProjectService systemProjectService;

    @Resource
    private OperationLogService operationLogService;


    @GetMapping("/get/options")
    @Operation(summary = "获取组织/项目级联下拉框选项")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_OPERATING_LOG_READ, PermissionConstants.ORGANIZATION_OPERATING_LOG_READ}, logical = Logical.OR)
    public OrganizationProjectOptionsResponse getOptions() {

        //获取全部组织
        List<OrganizationProjectOptionsDto> organizationList = organizationService.getOrganizationOptions();
        //获取全部项目
        List<OrganizationProjectOptionsDto> projectList = systemProjectService.getprojectOptions();

        OrganizationProjectOptionsResponse optionsResponse = new OrganizationProjectOptionsResponse();
        optionsResponse.setOrganizationList(organizationList);
        optionsResponse.setProjectList(projectList);

        return optionsResponse;
    }


    @PostMapping("/list")
    @Operation(summary = "操作日志列表查询")
    @RequiresPermissions(PermissionConstants.SYSTEM_OPERATING_LOG_READ)
    public Pager<List<OperationLogResponse>> list(@Validated @RequestBody OperationLogRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, operationLogService.list(request));
    }
}
