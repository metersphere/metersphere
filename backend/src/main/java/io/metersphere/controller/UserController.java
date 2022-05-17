package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.member.*;
import io.metersphere.controller.request.resourcepool.UserBatchProcessRequest;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserGroupPermissionDTO;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RequestMapping("user")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/special/add")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#user)", msClass = UserService.class)
    public UserDTO insertUser(@RequestBody UserRequest user) {
        return userService.insert(user);
    }

    @PostMapping("/special/list/{goPage}/{pageSize}")
    public Pager<List<User>> getUserList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody io.metersphere.controller.request.UserRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getUserListWithRequest(request));
    }

    @GetMapping("/special/user/group/{userId}")
    public UserGroupPermissionDTO getUserGroup(@PathVariable("userId") String userId) {
        return userService.getUserGroup(userId);
    }

    @GetMapping("/special/delete/{userId}")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#userId)", msClass = UserService.class)
    public void deleteUser(@PathVariable(value = "userId") String userId) {
        userService.deleteUser(userId);
        // 剔除在线用户
        SessionUtils.kickOutUser(userId);
    }

    @PostMapping("/special/update")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#user)", content = "#msClass.getLogDetails(#user)", msClass = UserService.class)
    public void updateUser(@RequestBody UserRequest user) {
        userService.updateUserRole(user);
    }

    @PostMapping("/special/update_status")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#user.id)", content = "#msClass.getLogDetails(#user.id)", msClass = UserService.class)
    public void updateStatus(@RequestBody User user) {
        userService.updateUser(user);
    }

    @PostMapping("/special/ws/member/list/{goPage}/{pageSize}")
    public Pager<List<User>> getMemberListByAdmin(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getMemberList(request));
    }

    @PostMapping("/special/ws/member/list/all")
    public List<User> getMemberListByAdmin(@RequestBody QueryMemberRequest request) {
        return userService.getMemberList(request);
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
    public List<User> getUserList() {
        return userService.getUserList();
    }

    @PostMapping("/update/current")
    @MsAuditLog(module = OperLogModule.PERSONAL_INFORMATION_PERSONAL_SETTINGS, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#user.id)", content = "#msClass.getLogDetails(#user.id)", msClass = UserService.class)
    public UserDTO updateCurrentUser(@RequestBody User user) {
        return userService.updateCurrentUser(user);
    }

    @GetMapping("/update/currentByResourceId/{resourceId}")
    public void updateCurrentUserByResourceId(@PathVariable String resourceId) {
        userService.updateCurrentUserByResourceId(resourceId);
    }

    @PostMapping("/switch/source/ws/{sourceId}")
    public UserDTO switchWorkspace(@PathVariable(value = "sourceId") String sourceId) {
        userService.switchUserResource("workspace", sourceId);
        return SessionUtils.getUser();
    }

    @PostMapping("/refresh/{sign}/{sourceId}")
    public UserDTO refreshSessionUser(@PathVariable String sign, @PathVariable String sourceId) {
        userService.refreshSessionUser(sign, sourceId);
        return SessionUtils.getUser();
    }

    @GetMapping("/info/{userId}")
    public UserDTO getUserInfo(@PathVariable(value = "userId") String userId) {
        return userService.getUserInfo(userId);
    }

    /**
     * 获取工作空间成员用户
     */
    @PostMapping("/ws/member/list/{goPage}/{pageSize}")
    public Pager<List<User>> getMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getMemberList(request));
    }

    @PostMapping("/project/member/list/{goPage}/{pageSize}")
    public Pager<List<User>> getProjectMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getProjectMemberList(request));
    }

    @PostMapping("/project/member/list")
    public List<User> getProjectMemberListAll(@RequestBody QueryMemberRequest request) {
        return userService.getProjectMemberList(request);
    }

    @GetMapping("/project/member/option/{projectId}")
    public List<User> getProjectMemberOption(@PathVariable String projectId) {
        return userService.getProjectMemberOption(projectId);
    }

    /**
     * 获取工作空间成员用户 不分页
     */
    @PostMapping("/ws/member/list/all")
    public List<User> getMemberList(@RequestBody QueryMemberRequest request) {
        return userService.getMemberList(request);
    }

    @GetMapping("/ws/current/member/list")
    public List<User> getCurrentWorkspaceMember() {
        QueryMemberRequest request = new QueryMemberRequest();
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return userService.getMemberList(request);
    }

    /**
     * 添加工作空间成员
     */
    @PostMapping("/ws/member/add")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MEMBER, type = OperLogConstants.CREATE, title = "添加工作空间成员")
    public void addMember(@RequestBody AddMemberRequest request) {

        String wsId = request.getWorkspaceId();
//        workspaceService.checkWorkspaceOwner(wsId);
        userService.addMember(request);
    }

    @PostMapping("/project/member/add")
//    @MsAuditLog(module = "workspace_member", type = OperLogConstants.CREATE, title = "添加项目成员成员")
    public void addProjectMember(@RequestBody AddMemberRequest request) {
//        workspaceService.checkWorkspaceOwner(wsId);
        userService.addProjectMember(request);
    }

    /**
     * 删除工作空间成员
     */
    @GetMapping("/ws/member/delete/{workspaceId}/{userId}")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MEMBER, type = OperLogConstants.DELETE, title = "删除工作空间成员")
    public void deleteMember(@PathVariable String workspaceId, @PathVariable String userId) {
//        workspaceService.checkWorkspaceOwner(workspaceId);
        String currentUserId = SessionUtils.getUser().getId();
        if (StringUtils.equals(userId, currentUserId)) {
            MSException.throwException(Translator.get("cannot_remove_current"));
        }
        userService.deleteMember(workspaceId, userId);
    }

    @GetMapping("/project/member/delete/{projectId}/{userId}")
//    @MsAuditLog(module = "workspace_member", type = OperLogConstants.DELETE, title = "删除工作空间成员")
    public void deleteProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        String currentUserId = SessionUtils.getUser().getId();
        if (StringUtils.equals(userId, currentUserId)) {
            MSException.throwException(Translator.get("cannot_remove_current"));
        }
        userService.deleteProjectMember(projectId, userId);
    }


    /**
     * ws 下所有相关人员
     */
    @GetMapping("/ws/member/list/{workspaceId}")
    public List<User> getWsMemberList(@PathVariable String workspaceId) {
        return userService.getWsAllMember(workspaceId);
    }

    /*
     * 修改当前用户密码
     * */
    @PostMapping("/update/password")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, title = "个人密码")
    public int updateCurrentUserPassword(@RequestBody EditPassWordRequest request) {
        return userService.updateCurrentUserPassword(request);
    }

    /*管理员修改用户密码*/
    @PostMapping("/special/password")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = UserService.class)
    public int updateUserPassword(@RequestBody EditPassWordRequest request) {
        return userService.updateUserPassword(request);
    }

    @PostMapping("/project/member/tester/list")
    public List<User> getProjectMember(@RequestBody QueryMemberRequest request) {
        return userService.getProjectMember(request);
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

    @PostMapping("/special/batchProcessUserInfo")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request)", content = "#msClass.getLogDetails(#request)", msClass = UserService.class)
    public String batchProcessUserInfo(@RequestBody UserBatchProcessRequest request) {
        String returnString = "success";
        userService.batchProcessUserInfo(request);
        return returnString;
    }

    /**
     * 根据userId 获取 user 所属工作空间和所属工作项目
     */
    @GetMapping("/get/ws_pj/{userId}")
    public Map<Object, Object> getWSAndProjectByUserId(@PathVariable String userId) {
        return userService.getWSAndProjectByUserId(userId);
    }

    /**
     * 配置 用户的selenium-server 地址 ip:port
     */
    @PostMapping("/update/seleniumServer")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, title = "selenium-server地址")
    public int updateSeleniumServer(@RequestBody EditSeleniumServerRequest request) {
        return userService.updateUserSeleniumServer(request);
    }

    @GetMapping("/verify/seleniumServer")
    public String verifySeleniumServer() {
        return userService.verifyUserSeleniumServer();
    }
}
