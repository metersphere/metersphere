package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserGroupPermissionDTO;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.request.member.*;
import io.metersphere.request.resourcepool.UserBatchProcessRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("user")
@RestController
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;

    @PostMapping("/special/add")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#user)", msClass = UserService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_CREATE)
    public UserDTO insertUser(@RequestBody UserRequest user) {
        return userService.insert(user);
    }

    @PostMapping("/special/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public Pager<List<User>> getUserList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody io.metersphere.request.UserRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getUserListWithRequest(request));
    }

    @GetMapping("/special/user/group/{userId}")
    public UserGroupPermissionDTO getUserGroup(@PathVariable("userId") String userId) {
        return userService.getUserGroup(userId);
    }

    @GetMapping("/special/delete/{userId}")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#userId)", msClass = UserService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_DELETE)
    public void deleteUser(@PathVariable(value = "userId") String userId) {
        userService.deleteUser(userId);
        // 剔除在线用户
        SessionUtils.kickOutUser(userId);
    }

    @PostMapping("/special/update")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#user)", content = "#msClass.getLogDetails(#user)", msClass = UserService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_EDIT)
    public void updateUser(@RequestBody UserRequest user) {
        userService.updateUserRole(user);
    }

    @PostMapping("/special/update_status")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#user.id)", content = "#msClass.getLogDetails(#user.id)", msClass = UserService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_EDIT)
    public void updateStatus(@RequestBody User user) {
        userService.updateUser(user);
    }

    @PostMapping("/special/ws/member/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.SYSTEM_WORKSPACE_READ)
    public Pager<List<User>> getMemberListByAdmin(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, baseUserService.getMemberList(request));
    }

    @PostMapping("/special/ws/member/list/all")
    @RequiresPermissions(PermissionConstants.SYSTEM_WORKSPACE_READ)
    public List<User> getMemberListByAdmin(@RequestBody QueryMemberRequest request) {
        return baseUserService.getMemberList(request);
    }

    @PostMapping("/special/ws/member/add")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MEMBER, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.userIds,#request.workspaceId)", msClass = UserService.class)
    public void addMemberByAdmin(@RequestBody AddMemberRequest request) {
        userService.addMember(request);
    }

    @GetMapping("/special/ws/member/delete/{workspaceId}/{userId}")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MEMBER, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#userId)", msClass = UserService.class)
    public void deleteMemberByAdmin(@PathVariable String workspaceId, @PathVariable String userId) {
        userService.deleteMember(workspaceId, userId);
    }

    @GetMapping("/list")
    @RequiresPermissions(value = {
            PermissionConstants.SYSTEM_WORKSPACE_READ_CREATE,
            PermissionConstants.SYSTEM_GROUP_READ_CREATE,
            PermissionConstants.WORKSPACE_USER_READ_CREATE,
            PermissionConstants.SYSTEM_OPERATING_LOG_READ,
    }, logical = Logical.OR)
    public List<User> getUserList() {
        return userService.getUserList();
    }

    @PostMapping("/refresh/{sign}/{sourceId}")
    public UserDTO refreshSessionUser(@PathVariable String sign, @PathVariable String sourceId) {
        userService.refreshSessionUser(sign, sourceId);
        return SessionUtils.getUser();
    }

    /**
     * 获取工作空间成员用户
     */
    @PostMapping("/ws/member/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.WORKSPACE_USER_READ)
    public Pager<List<User>> getMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, baseUserService.getMemberList(request));
    }

    @PostMapping("/project/member/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_USER_READ)
    public Pager<List<User>> getProjectMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getProjectMemberList(request));
    }

    @PostMapping("/ws/project/member/list/{workspaceId}/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ)
    public Pager<List<User>> getProjectMemberListForWorkspace(@PathVariable int goPage, @PathVariable int pageSize, @PathVariable String workspaceId, @RequestBody QueryMemberRequest request) {
        baseCheckPermissionService.checkProjectBelongToWorkspace(request.getProjectId(), workspaceId);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getProjectMemberList(request));
    }

    /**
     * 添加工作空间成员
     */
    @PostMapping("/ws/member/add")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MEMBER, type = OperLogConstants.CREATE, title = "添加工作空间成员")
    @RequiresPermissions(PermissionConstants.WORKSPACE_USER_READ_CREATE)
    public void addMember(@RequestBody AddMemberRequest request) {
        userService.addWorkspaceMember(request);
    }

    @PostMapping("/project/member/add")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_USER_READ_CREATE, PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_ADD_USER}, logical = Logical.OR)
    @MsRequestLog(module = OperLogModule.PROJECT_PROJECT_MEMBER)
    public void addProjectMember(@RequestBody AddMemberRequest request) {
        userService.addProjectMember(request);
    }

    /**
     * 删除工作空间成员
     */
    @GetMapping("/ws/member/delete/{workspaceId}/{userId}")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MEMBER, type = OperLogConstants.DELETE, title = "删除工作空间成员")
    @RequiresPermissions(PermissionConstants.WORKSPACE_USER_READ_DELETE)
    public void deleteMember(@PathVariable String workspaceId, @PathVariable String userId) {
//        workspaceService.checkWorkspaceOwner(workspaceId);
        String currentUserId = SessionUtils.getUser().getId();
        if (StringUtils.equals(userId, currentUserId)) {
            MSException.throwException(Translator.get("cannot_remove_current"));
        }
        userService.deleteMember(workspaceId, userId);
    }

    @GetMapping("/project/member/delete/{projectId}/{userId}")
    @RequiresPermissions(value={PermissionConstants.PROJECT_USER_READ_DELETE, PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_DELETE_USER}, logical = Logical.OR)
    @MsRequestLog(module = OperLogModule.PROJECT_PROJECT_MEMBER)
    public void deleteProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        String currentUserId = SessionUtils.getUser().getId();
        if (StringUtils.equals(userId, currentUserId)) {
            MSException.throwException(Translator.get("cannot_remove_current"));
        }
        userService.deleteProjectMember(projectId, userId);
    }


    /*管理员修改用户密码*/
    @PostMapping("/special/password")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = UserService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_EDIT_PASSWORD)
    public int updateUserPassword(@RequestBody EditPassWordRequest request) {
        return userService.updateUserPassword(request);
    }

    @GetMapping("/search/{condition}")
    public List<User> searchUser(@PathVariable String condition) {
        return userService.searchUser(condition);
    }

    @GetMapping("/export/template")
    public void testCaseTemplateExport(HttpServletResponse response) {
        userService.userTemplateExport(response);
    }

    @PostMapping("/import")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.IMPORT)
    public ExcelResponse testCaseImport(MultipartFile file, HttpServletRequest request) {
        return userService.userImport(file, request);
    }

    @PostMapping("/special/batch-process-user")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request)", content = "#msClass.getLogDetails(#request)", msClass = UserService.class)
    public String batchProcessUserInfo(@RequestBody UserBatchProcessRequest request) {
        String returnString = "success";
        userService.batchProcessUserInfo(request);
        return returnString;
    }


}
