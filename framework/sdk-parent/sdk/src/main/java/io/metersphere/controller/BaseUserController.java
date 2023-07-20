package io.metersphere.controller;

import io.metersphere.base.domain.User;
import io.metersphere.base.domain.UserGroup;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.UserGroupConstants;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.UserDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.AuthUserIssueRequest;
import io.metersphere.request.member.EditPassWordRequest;
import io.metersphere.request.member.EditSeleniumServerRequest;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.service.BaseUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/user")
public class BaseUserController {
    @Resource
    private BaseUserService baseUserService;

    @GetMapping("/ws/current/member/list")
    public List<User> getCurrentWorkspaceMember() {
        SessionUser user = SessionUtils.getUser();
        Optional<UserGroup> any = user.getUserGroups().stream()
                .filter(ug -> (ug.getSourceId().equals(SessionUtils.getCurrentWorkspaceId()) || ug.getGroupId().equals(UserGroupConstants.SUPER_GROUP)))
                .findAny();
        if (any.isEmpty()) {
            return new ArrayList<>();
        }
        QueryMemberRequest request = new QueryMemberRequest();
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return baseUserService.getMemberList(request);
    }


    @GetMapping("/switch/source/ws/{sourceId}")
    public UserDTO switchWorkspace(@PathVariable(value = "sourceId") String sourceId) {
        baseUserService.switchUserResource("workspace", sourceId, Objects.requireNonNull(SessionUtils.getUser()));
        return SessionUtils.getUser();
    }

    @PostMapping("/update/current")
    @MsAuditLog(module = OperLogModule.PERSONAL_INFORMATION_PERSONAL_SETTINGS, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#user.id)", content = "#msClass.getLogDetails(#user.id)", msClass = BaseUserService.class)
    public UserDTO updateCurrentUser(@RequestBody User user) {
        return baseUserService.updateCurrentUser(user);
    }

    /*
     * 修改当前用户密码
     * */
    @PostMapping("/update/password")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, title = "个人密码")
    public int updateCurrentUserPassword(@RequestBody EditPassWordRequest request) {
        return baseUserService.updateCurrentUserPassword(request);
    }


    @GetMapping("/project/member/list")
    public List<User> getProjectMemberListAll() {
        SessionUser user = SessionUtils.getUser();
        Optional<UserGroup> any = user.getUserGroups().stream()
                .filter(ug -> (ug.getSourceId().equals(SessionUtils.getCurrentProjectId()) || ug.getGroupId().equals(UserGroupConstants.SUPER_GROUP)))
                .findAny();
        if (any.isEmpty()) {
            return new ArrayList<>();
        }
        QueryMemberRequest request = new QueryMemberRequest();
        request.setProjectId(SessionUtils.getCurrentProjectId());
        return baseUserService.getProjectMemberList(request);
    }

    @GetMapping("/project/member/option")
    public List<User> getProjectMemberOption() {
        return baseUserService.getProjectMemberOption(SessionUtils.getCurrentProjectId());
    }

    @GetMapping("/project/member/{projectId}")
    public List<User> getProjectMembers(@PathVariable String projectId) {
        SessionUser user = SessionUtils.getUser();
        Optional<UserGroup> any = user.getUserGroups().stream()
                .filter(ug -> (ug.getSourceId().equals(projectId) || ug.getGroupId().equals(UserGroupConstants.SUPER_GROUP)))
                .findAny();
        if (any.isEmpty()) {
            return new ArrayList<>();
        }
        QueryMemberRequest request = new QueryMemberRequest();
        request.setProjectId(projectId);
        return baseUserService.getProjectMemberList(request);
    }

    @GetMapping("/info")
    public UserDTO getUserInfo() {
        return baseUserService.getUserDTO(SessionUtils.getUserId());
    }

    /**
     * 根据userId 获取 user 所属工作空间和所属工作项目
     */
    @GetMapping("/get/ws-pj/{userId}")
    public Map<Object, Object> getWSAndProjectByUserId(@PathVariable String userId) {
        return baseUserService.getWSAndProjectByUserId(userId);
    }


    /**
     * 配置 用户的selenium-server 地址 ip:port
     */
    @PostMapping("/update/selenium-server")
    @MsAuditLog(module = OperLogModule.SYSTEM_USER, type = OperLogConstants.UPDATE, title = "selenium-server地址")
    public int updateSeleniumServer(@RequestBody EditSeleniumServerRequest request) {
        return baseUserService.updateUserSeleniumServer(request);
    }

    @PostMapping("issue/auth")
    public void userAuth(@RequestBody AuthUserIssueRequest authUserIssueRequest) {
        baseUserService.userIssueAuth(authUserIssueRequest);
    }

    @GetMapping("/update/current-by-resource/{resourceId}")
    public void updateCurrentUserByResourceId(@PathVariable String resourceId) {
        baseUserService.updateCurrentUserByResourceId(resourceId);
    }

    @GetMapping("/is/super/{userid}")
    public boolean isSuperUser(@PathVariable String userid) {
        return baseUserService.isSuperUser(userid);
    }
}
