package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.log.vo.OperationLogRequest;
import io.metersphere.sdk.log.vo.OperationLogResponse;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import io.metersphere.system.dto.response.OrganizationProjectOptionsResponse;
import io.metersphere.system.service.SystemProjectService;
import io.metersphere.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author wx
 */
@Tag(name = "组织日志")
@RestController
@RequestMapping("/organization/log")
public class OrganizationLogController {

    @Resource
    private SystemProjectService systemProjectService;

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private UserService userService;


    @GetMapping("/get/options/{organizationId}")
    @Operation(summary = "组织日志-获取项目级联下拉框选项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_OPERATING_LOG_READ)
    public OrganizationProjectOptionsResponse getOrganizationOptions(@PathVariable(value = "organizationId") String organizationId) {
        //获取全部项目
        List<OrganizationProjectOptionsDTO> projectList = systemProjectService.getProjectOptions(organizationId);
        OrganizationProjectOptionsResponse optionsResponse = new OrganizationProjectOptionsResponse();
        optionsResponse.setProjectList(projectList);

        return optionsResponse;
    }


    @GetMapping("/user/list/{organizationId}")
    @Operation(summary = "组织日志-获取用户列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_OPERATING_LOG_READ)
    public List<User> getLogUserList(@PathVariable(value = "organizationId") String organizationId) {
        return userService.getUserListByOrgId(organizationId);
    }


    @PostMapping("/list")
    @Operation(summary = "组织菜单下操作日志列表查询")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_OPERATING_LOG_READ)
    public Pager<List<OperationLogResponse>> organizationLogList(@Validated @RequestBody OperationLogRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, operationLogService.list(request));
    }

}
