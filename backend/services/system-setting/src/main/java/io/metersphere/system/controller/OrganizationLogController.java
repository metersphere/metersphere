package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import io.metersphere.system.dto.response.OrganizationProjectOptionsResponse;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.log.vo.OperationLogResponse;
import io.metersphere.system.log.vo.OrgOperationLogRequest;
import io.metersphere.system.log.vo.SystemOperationLogRequest;
import io.metersphere.system.service.SimpleUserService;
import io.metersphere.system.service.SystemProjectService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 * @author wx
 */
@Tag(name = "系统设置-组织-日志")
@RestController
@RequestMapping("/organization/log")
public class OrganizationLogController {

    @Resource
    private SystemProjectService systemProjectService;

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private SimpleUserService simpleUserService;


    @GetMapping("/get/options/{organizationId}")
    @Operation(summary = "系统设置-组织-日志-获取项目级联下拉框选项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_LOG_READ)
    public OrganizationProjectOptionsResponse getOrganizationOptions(@PathVariable(value = "organizationId") String organizationId) {
        //获取全部项目
        List<OrganizationProjectOptionsDTO> projectList = systemProjectService.getProjectOptions(organizationId);
        OrganizationProjectOptionsResponse optionsResponse = new OrganizationProjectOptionsResponse();
        optionsResponse.setProjectList(projectList);

        return optionsResponse;
    }


    @GetMapping("/user/list/{organizationId}")
    @Operation(summary = "系统设置-组织-日志-获取用户列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_LOG_READ)
    public List<User> getLogUserList(@PathVariable(value = "organizationId") String organizationId,
                                     @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                     @RequestParam(value = "keyword", required = false) String keyword) {
        return simpleUserService.getUserListByOrgId(organizationId, keyword);
    }


    @PostMapping("/list")
    @Operation(summary = "系统设置-组织-日志-组织菜单下操作日志列表查询")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_LOG_READ)
    public Pager<List<OperationLogResponse>> organizationLogList(@Validated @RequestBody OrgOperationLogRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        SystemOperationLogRequest operationLogRequest = new SystemOperationLogRequest();
        BeanUtils.copyBean(operationLogRequest, request);
        operationLogRequest.setOrganizationIds(Arrays.asList(SessionUtils.getCurrentOrganizationId()));
        return PageUtils.setPageInfo(page, operationLogService.list(operationLogRequest));
    }

}
