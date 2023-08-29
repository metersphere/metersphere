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
import io.metersphere.system.service.OrganizationService;
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

@Tag(name = "系统设置-系统-日志")
@RestController
@RequestMapping("/operation/log")
public class OperationLogController {

    @Resource
    private OrganizationService organizationService;

    @Resource
    private SystemProjectService systemProjectService;

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private UserService userService;


    @GetMapping("/get/options")
    @Operation(summary = "系统设置-系统-日志-获取组织/项目级联下拉框选项")
    @RequiresPermissions(PermissionConstants.SYSTEM_LOG_READ)
    public OrganizationProjectOptionsResponse getOptions() {

        //获取全部组织
        List<OrganizationProjectOptionsDTO> organizationList = organizationService.getOrganizationOptions();
        //获取全部项目
        List<OrganizationProjectOptionsDTO> projectList = systemProjectService.getProjectOptions(null);

        OrganizationProjectOptionsResponse optionsResponse = new OrganizationProjectOptionsResponse();
        optionsResponse.setOrganizationList(organizationList);
        optionsResponse.setProjectList(projectList);

        return optionsResponse;
    }


    @PostMapping("/list")
    @Operation(summary = "系统设置-系统-日志-系统操作日志列表查询")
    @RequiresPermissions(PermissionConstants.SYSTEM_LOG_READ)
    public Pager<List<OperationLogResponse>> list(@Validated @RequestBody OperationLogRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, operationLogService.list(request));
    }


    @GetMapping("/user/list")
    @Operation(summary = "系统设置-系统-日志-系统日志页面，获取用户列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_LOG_READ)
    public List<User> getUserList() {
        List<User> userList = userService.getUserList();
        return userList;
    }
}
