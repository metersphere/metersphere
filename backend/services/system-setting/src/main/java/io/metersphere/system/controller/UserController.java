package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.UserSourceEnum;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.dto.UserRoleOption;
import io.metersphere.system.dto.request.UserBaseBatchRequest;
import io.metersphere.system.dto.request.UserChangeEnableRequest;
import io.metersphere.system.dto.request.UserEditRequest;
import io.metersphere.system.dto.request.user.UserAndRoleBatchRequest;
import io.metersphere.system.dto.response.UserBatchProcessResponse;
import io.metersphere.system.dto.response.UserImportResponse;
import io.metersphere.system.dto.response.UserTableResponse;
import io.metersphere.system.service.GlobalUserRoleRelationLogService;
import io.metersphere.system.service.GlobalUserRoleRelationService;
import io.metersphere.system.service.GlobalUserRoleService;
import io.metersphere.system.service.UserService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/system/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private GlobalUserRoleService globalUserRoleService;
    @Resource
    private GlobalUserRoleRelationService globalUserRoleRelationService;

    @GetMapping("/get/{email}")
    @Operation(summary = "通过email查找用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public UserDTO getUser(@PathVariable String email) {
        return userService.getUserDTOByEmail(email);
    }

    @GetMapping("/get/global/system/role")
    @Operation(summary = "查找系统级用户权限")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public List<UserRoleOption> getGlobalSystemRole() {
        return globalUserRoleService.getGlobalSystemRoleList();
    }

    @PostMapping("/add")
    @Operation(summary = "添加用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_ADD)
    public UserBatchCreateDTO addUser(@Validated({Created.class}) @RequestBody UserBatchCreateDTO userCreateDTO) {
        return userService.addUser(userCreateDTO, UserSourceEnum.LOCAL.name(), SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "修改用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = UserService.class)
    public UserEditRequest updateUser(@Validated({Updated.class}) @RequestBody UserEditRequest request) {
        return userService.updateUser(request, SessionUtils.getUserId());
    }

    @PostMapping("/page")
    @Operation(summary = "分页查找用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public Pager<List<UserTableResponse>> list(@Validated @RequestBody BasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, userService.list(request));
    }

    @PostMapping("/update/enable")
    @Operation(summary = "启用/禁用用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchUpdateLog(#request)", msClass = UserService.class)
    public UserBatchProcessResponse updateUserEnable(@Validated @RequestBody UserChangeEnableRequest request) {
        return userService.updateUserEnable(request, SessionUtils.getSessionId());
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @Operation(summary = "导入用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_IMPORT)
    public UserImportResponse importUser(@RequestPart(value = "file", required = false) MultipartFile excelFile) {
        return userService.importByExcel(excelFile, UserSourceEnum.LOCAL.name(), SessionUtils.getSessionId());
    }

    @PostMapping("/delete")
    @Operation(summary = "删除用户")
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#request)", msClass = UserService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_DELETE)
    public UserBatchProcessResponse deleteUser(@Validated @RequestBody UserBaseBatchRequest request) {
        return userService.deleteUser(request, SessionUtils.getUserId());
    }

    @GetMapping("/list")
    @Operation(summary = "系统/组织日志页面，获取用户列表")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_OPERATING_LOG_READ, PermissionConstants.ORGANIZATION_OPERATING_LOG_READ}, logical = Logical.OR)
    public List<User> getUserList() {
        return userService.getUserList();
    }

    @PostMapping("/reset/password")
    @Operation(summary = "重置用户密码")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.resetPasswordLog(#request)", msClass = UserService.class)
    public UserBatchProcessResponse resetPassword(@Validated @RequestBody UserBaseBatchRequest request) {
        return userService.resetPassword(request, SessionUtils.getUserId());
    }

    @GetMapping("/get-option/{sourceId}")
    @Operation(summary = "系统-组织及项目, 获取用户下拉选项")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_USER_READ})
    @Parameter(name = "sourceId", description = "组织ID或项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public List<UserExtend> getMemberOption(@PathVariable String sourceId) {
        return userService.getMemberOption(sourceId);
    }

    @PostMapping("/add/batch/user-role")
    @Operation(summary = "批量添加用户到多个用户组中")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#msClass.batchAddLog(#request)", msClass = GlobalUserRoleRelationLogService.class)
    public UserBatchProcessResponse batchAdd(@Validated({Created.class}) @RequestBody UserAndRoleBatchRequest request) {
        return globalUserRoleRelationService.batchAdd(request, SessionUtils.getUserId());
    }
}
