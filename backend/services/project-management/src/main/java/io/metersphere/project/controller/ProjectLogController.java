package io.metersphere.project.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.log.vo.OperationLogRequest;
import io.metersphere.sdk.log.vo.OperationLogResponse;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 * @author wx
 */

@Tag(name = "项目管理-日志")
@RestController
@RequestMapping("/project/log")
public class ProjectLogController {

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/user/list/{projectId}")
    @Operation(summary = "项目管理-日志-获取用户列表-支持远程搜索")
    @RequiresPermissions(PermissionConstants.PROJECT_LOG_READ)
    public List<User> getUserList(@PathVariable(value = "projectId") String projectId,
                                  @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                  @RequestParam(value = "keyword", required = false) String keyword) {
        return userService.getUserListByOrgId(StringUtils.defaultIfBlank(projectId,SessionUtils.getCurrentProjectId()), keyword);
    }


    @PostMapping("/list")
    @Operation(summary = "项目管理-日志--操作日志列表查询")
    @RequiresPermissions(PermissionConstants.PROJECT_LOG_READ)
    public Pager<List<OperationLogResponse>> projectLogList(@Validated @RequestBody OperationLogRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        if (CollectionUtils.isEmpty(request.getProjectIds())) {
            //未传项目id 获取登录用户当前项目id
            request.setProjectIds(Arrays.asList(SessionUtils.getCurrentProjectId()));
        }
        return PageUtils.setPageInfo(page, operationLogService.list(request));
    }

}
