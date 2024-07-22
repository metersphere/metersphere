package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.EmailInviteSource;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.UserSource;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.dto.request.OrganizationMemberBatchRequest;
import io.metersphere.system.dto.request.ProjectAddMemberBatchRequest;
import io.metersphere.system.dto.request.UserInviteRequest;
import io.metersphere.system.dto.request.UserRegisterRequest;
import io.metersphere.system.dto.request.user.UserChangeEnableRequest;
import io.metersphere.system.dto.request.user.UserEditRequest;
import io.metersphere.system.dto.request.user.UserRoleBatchRelationRequest;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.table.TableBatchProcessResponse;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.request.UserBatchCreateRequest;
import io.metersphere.system.dto.user.response.*;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.*;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.system.utils.TreeNodeParseUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "系统设置-系统-用户")
@RequestMapping("/system/user")
public class UserController {
    @Resource
    private SimpleUserService simpleUserService;
    @Resource
    private UserToolService userToolService;
    @Resource
    private GlobalUserRoleService globalUserRoleService;
    @Resource
    private GlobalUserRoleRelationService globalUserRoleRelationService;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private SystemProjectService systemProjectService;
    @Resource
    private UserLogService userLogService;

    @GetMapping("/get/{keyword}")
    @Operation(summary = "通过email或id查找用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public UserDTO getUser(@PathVariable String keyword) {
        return simpleUserService.getUserDTOByKeyword(keyword);
    }

    @PostMapping("/add")
    @Operation(summary = "系统设置-系统-用户-添加用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ADD)
    public UserBatchCreateResponse addUser(@Validated({Created.class}) @RequestBody UserBatchCreateRequest userCreateDTO) {
        return simpleUserService.addUser(userCreateDTO, UserSource.LOCAL.name(), SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-用户-修改用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = UserLogService.class)
    public UserEditRequest updateUser(@Validated({Updated.class}) @RequestBody UserEditRequest request) {
        return simpleUserService.updateUser(request, SessionUtils.getUserId());
    }

    @PostMapping("/page")
    @Operation(summary = "系统设置-系统-用户-分页查找用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public Pager<List<UserTableResponse>> list(@Validated @RequestBody BasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "create_time desc,id desc");
        return PageUtils.setPageInfo(page, simpleUserService.list(request));
    }

    @PostMapping("/update/enable")
    @Operation(summary = "系统设置-系统-用户-启用/禁用用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchUpdateEnableLog(#request)", msClass = UserLogService.class)
    public TableBatchProcessResponse updateUserEnable(@Validated @RequestBody UserChangeEnableRequest request) {
        return simpleUserService.updateUserEnable(request, SessionUtils.getUserId(), SessionUtils.getUser().getName());
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @Operation(summary = "系统设置-系统-用户-导入用户")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_IMPORT)
    public UserImportResponse importUser(@RequestPart(value = "file", required = false) MultipartFile excelFile) {
        return simpleUserService.importByExcel(excelFile, UserSource.LOCAL.name(), SessionUtils.getUserId());
    }

    @PostMapping("/delete")
    @Operation(summary = "系统设置-系统-用户-删除用户")
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#request)", msClass = UserLogService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_DELETE)
    public TableBatchProcessResponse deleteUser(@Validated @RequestBody TableBatchProcessDTO request) {
        return simpleUserService.deleteUser(request, SessionUtils.getUserId(), SessionUtils.getUser().getName());
    }

    @PostMapping("/reset/password")
    @Operation(summary = "系统设置-系统-用户-重置用户密码")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.resetPasswordLog(#request)", msClass = UserLogService.class)
    public TableBatchProcessResponse resetPassword(@Validated @RequestBody TableBatchProcessDTO request) {
        return simpleUserService.resetPassword(request, SessionUtils.getUserId());
    }

    @GetMapping("/get/global/system/role")
    @Operation(summary = "系统设置-系统-用户-查找系统级用户组")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public List<UserSelectOption> getGlobalSystemRole() {
        return globalUserRoleService.getGlobalSystemRoleList();
    }

    @GetMapping("/get/organization")
    @Operation(summary = "系统设置-系统-用户-用户批量操作-查找组织")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_USER_ROLE_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ}, logical = Logical.AND)
    public List<OptionDTO> getOrganization() {
        return organizationService.listAll();
    }

    @GetMapping("/get/project")
    @Operation(summary = "系统设置-系统-用户-用户批量操作-查找项目")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_USER_ROLE_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ}, logical = Logical.AND)
    public List<BaseTreeNode> getProject() {
        Map<Organization, List<Project>> orgProjectMap = organizationService.getOrgProjectMap();
        return TreeNodeParseUtils.parseOrgProjectMap(orgProjectMap);
    }

    @PostMapping("/add/batch/user-role")
    @Operation(summary = "系统设置-系统-用户-批量添加用户到多个用户组中")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_UPDATE)
    public TableBatchProcessResponse batchAddUserGroupRole(@Validated({Created.class}) @RequestBody UserRoleBatchRelationRequest request) {
        TableBatchProcessResponse returnResponse = globalUserRoleRelationService.batchAdd(request, SessionUtils.getUserId());
        userLogService.batchAddUserRoleLog(request, SessionUtils.getUserId());
        return returnResponse;
    }

    @PostMapping("/add-project-member")
    @Operation(summary = "系统设置-系统-用户-批量添加用户到项目")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_USER_UPDATE, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD}, logical = Logical.AND)
    public TableBatchProcessResponse addProjectMember(@Validated @RequestBody UserRoleBatchRelationRequest userRoleBatchRelationRequest) {
        ProjectAddMemberBatchRequest request = new ProjectAddMemberBatchRequest();
        request.setProjectIds(userRoleBatchRelationRequest.getRoleIds());
        request.setUserIds(userRoleBatchRelationRequest.getSelectIds());
        systemProjectService.addProjectMember(request, SessionUtils.getUserId());
        userLogService.batchAddProjectLog(userRoleBatchRelationRequest, SessionUtils.getUserId());
        return new TableBatchProcessResponse(userRoleBatchRelationRequest.getSelectIds().size(), userRoleBatchRelationRequest.getSelectIds().size());
    }

    @PostMapping("/add-org-member")
    @Operation(summary = "系统设置-系统-用户-批量添加用户到组织")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_USER_UPDATE, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD}, logical = Logical.AND)
    public TableBatchProcessResponse addMember(@Validated @RequestBody UserRoleBatchRelationRequest userRoleBatchRelationRequest) {
        //获取本次处理的用户
        userRoleBatchRelationRequest.setSelectIds(userToolService.getBatchUserIds(userRoleBatchRelationRequest));
        OrganizationMemberBatchRequest request = new OrganizationMemberBatchRequest();
        request.setOrganizationIds(userRoleBatchRelationRequest.getRoleIds());
        request.setUserIds(userRoleBatchRelationRequest.getSelectIds());
        organizationService.addMemberBySystem(request, SessionUtils.getUserId());
        userLogService.batchAddOrgLog(userRoleBatchRelationRequest, SessionUtils.getUserId());
        return new TableBatchProcessResponse(userRoleBatchRelationRequest.getSelectIds().size(), userRoleBatchRelationRequest.getSelectIds().size());
    }

    @PostMapping("/invite")
    @Operation(summary = "系统设置-系统-用户-邀请用户注册")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_INVITE)
    public UserInviteResponse invite(@Validated @RequestBody UserInviteRequest request) {
        return simpleUserService.saveInviteRecord(request, EmailInviteSource.SYSTEM.name(), SessionUtils.getUser());
    }

    @GetMapping("/check-invite/{inviteId}")
    @Operation(summary = "系统设置-系统-用户-用户接受注册邀请并创建账户")
    public void checkInviteNum(@PathVariable String inviteId) {
        simpleUserService.getUserInviteAndCheckEfficient(inviteId);
    }

    @PostMapping("/register-by-invite")
    @Operation(summary = "系统设置-系统-用户-用户接受注册邀请并创建账户")
    public String registerByInvite(@Validated @RequestBody UserRegisterRequest request) throws Exception {
        return simpleUserService.registerByInvite(request);
    }
}
