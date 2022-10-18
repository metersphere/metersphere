package io.metersphere.gateway.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.constants.UserStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.GroupResourceDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserGroupPermissionDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.request.LoginRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserLoginService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private UserGroupPermissionMapper userGroupPermissionMapper;
    @Resource
    private ProjectMapper projectMapper;

    public Optional<SessionUser> login(LoginRequest request, WebSession session, Locale locale) {
        UserDTO userDTO;
        if (locale != null) {
            LocaleContextHolder.setLocale(locale, true);
        }
        switch (request.getAuthenticate()) {
            case "OIDC":
            case "CAS":
                userDTO = loginSsoMode(request.getUsername(), request.getAuthenticate());
                break;
            case "LDAP":
                userDTO = loginLdapMode(request.getUsername(), request.getAuthenticate());
                break;
            default:
                userDTO = loginLocalMode(request.getUsername(), request.getPassword());
                break;
        }
        autoSwitch(userDTO);
        return Optional.of(SessionUser.fromUser(userDTO, session.getId()));
    }

    private UserDTO loginLdapMode(String userId, String authenticate) {
        return getLoginUser(userId, Collections.singletonList(authenticate));
    }

    private UserDTO loginSsoMode(String userId, String authType) {
        return getLoginUser(userId, Collections.singletonList(authType));
    }

    public UserDTO loginLocalMode(String userId, String password) {
        UserDTO user = getLoginUser(userId, Collections.singletonList(UserSource.LOCAL.name()));
        if (user == null) {
            user = getUserDTOByEmail(userId, UserSource.LOCAL.name());
            if (user == null) {
                throw new RuntimeException(Translator.get("password_is_incorrect"));
            }
            userId = user.getId();
        }
        // 密码验证
        if (!checkUserPassword(userId, password)) {
            throw new RuntimeException(Translator.get("password_is_incorrect"));
        }
        user.setPassword(null);
        return user;
    }

    public void autoSwitch(UserDTO user) {
        // 用户有 last_project_id 权限
        if (hasLastProjectPermission(user)) {
            return;
        }
        // 用户有 last_workspace_id 权限
        if (hasLastWorkspacePermission(user)) {
            return;
        }
        // 判断其他权限
        checkNewWorkspaceAndProject(user);
    }

    private boolean hasLastProjectPermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastProjectId())) {
            List<UserGroup> projectUserGroups = user.getUserGroups().stream()
                    .filter(ug -> StringUtils.equals(user.getLastProjectId(), ug.getSourceId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(projectUserGroups)) {
                Project project = projectMapper.selectByPrimaryKey(user.getLastProjectId());
                if (StringUtils.equals(project.getWorkspaceId(), user.getLastWorkspaceId())) {
                    return true;
                }
                // last_project_id 和 last_workspace_id 对应不上了
                user.setLastWorkspaceId(project.getWorkspaceId());
                updateUser(user);
                return true;
            }
        }
        return false;
    }

    private boolean hasLastWorkspacePermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastWorkspaceId())) {
            List<UserGroup> workspaceUserGroups = user.getUserGroups().stream()
                    .filter(ug -> StringUtils.equals(user.getLastWorkspaceId(), ug.getSourceId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(workspaceUserGroups)) {
                ProjectExample example = new ProjectExample();
                example.createCriteria().andWorkspaceIdEqualTo(user.getLastWorkspaceId());
                List<Project> projects = projectMapper.selectByExample(example);
                // 工作空间下没有项目
                if (CollectionUtils.isEmpty(projects)) {
                    return true;
                }
                // 工作空间下有项目，选中有权限的项目
                List<String> projectIds = projects.stream()
                        .map(Project::getId)
                        .collect(Collectors.toList());

                List<UserGroup> userGroups = user.getUserGroups();
                List<String> projectGroupIds = user.getGroups()
                        .stream().filter(ug -> StringUtils.equals(ug.getType(), UserGroupType.PROJECT))
                        .map(Group::getId)
                        .collect(Collectors.toList());
                List<String> projectIdsWithPermission = userGroups.stream().filter(ug -> projectGroupIds.contains(ug.getGroupId()))
                        .filter(p -> StringUtils.isNotBlank(p.getSourceId()))
                        .map(UserGroup::getSourceId)
                        .filter(projectIds::contains)
                        .collect(Collectors.toList());

                List<String> intersection = projectIds.stream().filter(projectIdsWithPermission::contains).collect(Collectors.toList());
                // 当前工作空间下的所有项目都没有权限
                if (CollectionUtils.isEmpty(intersection)) {
                    return true;
                }
                Project project = projects.stream().filter(p -> StringUtils.equals(intersection.get(0), p.getId())).findFirst().get();
                String wsId = project.getWorkspaceId();
                user.setId(user.getId());
                user.setLastProjectId(project.getId());
                user.setLastWorkspaceId(wsId);
                updateUser(user);
                return true;
            }
        }
        return false;
    }

    private void checkNewWorkspaceAndProject(UserDTO user) {
        List<UserGroup> userGroups = user.getUserGroups();
        List<String> projectGroupIds = user.getGroups()
                .stream().filter(ug -> StringUtils.equals(ug.getType(), UserGroupType.PROJECT))
                .map(Group::getId)
                .collect(Collectors.toList());
        List<UserGroup> project = userGroups.stream().filter(ug -> projectGroupIds.contains(ug.getGroupId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(project)) {
            List<String> workspaceIds = user.getGroups()
                    .stream()
                    .filter(ug -> StringUtils.equals(ug.getType(), UserGroupType.WORKSPACE))
                    .map(Group::getId)
                    .collect(Collectors.toList());
            List<UserGroup> workspaces = userGroups.stream().filter(ug -> workspaceIds.contains(ug.getGroupId()))
                    .collect(Collectors.toList());
            if (workspaces.size() > 0) {
                String wsId = workspaces.get(0).getSourceId();
                switchUserResource("workspace", wsId, user);
            } else {
                // 用户登录之后没有项目和工作空间的权限就把值清空
                user.setLastWorkspaceId("");
                user.setLastProjectId("");
                updateUser(user);
            }
        } else {
            UserGroup userGroup = project.stream().filter(p -> StringUtils.isNotBlank(p.getSourceId()))
                    .collect(Collectors.toList()).get(0);
            String projectId = userGroup.getSourceId();
            Project p = projectMapper.selectByPrimaryKey(projectId);
            String wsId = p.getWorkspaceId();
            user.setId(user.getId());
            user.setLastProjectId(projectId);
            user.setLastWorkspaceId(wsId);
            updateUser(user);
        }
    }

    public void switchUserResource(String sign, String sourceId, UserDTO sessionUser) {
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();

        if (StringUtils.equals("workspace", sign)) {
            user.setLastWorkspaceId(sourceId);
            sessionUser.setLastWorkspaceId(sourceId);
            List<Project> projects = getProjectListByWsAndUserId(sessionUser.getId(), sourceId);
            if (projects.size() > 0) {
                user.setLastProjectId(projects.get(0).getId());
            } else {
                user.setLastProjectId("");
            }
        }
        BeanUtils.copyProperties(user, newUser);
        // 切换工作空间或组织之后更新 session 里的 user
        SessionUtils.putUser(SessionUser.fromUser(user, SessionUtils.getSessionId()));
        userMapper.updateByPrimaryKeySelective(newUser);
    }
    public UserDTO getLoginUser(String userId, List<String> list) {
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andSourceIn(list);
        if (userMapper.countByExample(example) == 0) {
            return null;
        }
        return getUserDTO(userId);
    }

    public UserDTO getUserDTOByEmail(String email, String... source) {
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(email);

        if (!CollectionUtils.isEmpty(Arrays.asList(source))) {
            criteria.andSourceIn(Arrays.asList(source));
        }

        List<User> users = userMapper.selectByExample(example);

        if (users == null || users.size() <= 0) {
            return null;
        }

        return getUserDTO(users.get(0).getId());
    }

    public UserDTO getUserDTO(String userId) {

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return null;
        }
        if (StringUtils.equals(user.getStatus(), UserStatus.DISABLED)) {
            throw new RuntimeException(Translator.get("user_has_been_disabled"));
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        UserGroupPermissionDTO dto = getUserGroupPermission(userId);
        userDTO.setUserGroups(dto.getUserGroups());
        userDTO.setGroups(dto.getGroups());
        userDTO.setGroupPermissions(dto.getList());
        return userDTO;
    }

    private UserGroupPermissionDTO getUserGroupPermission(String userId) {
        UserGroupPermissionDTO permissionDTO = new UserGroupPermissionDTO();
        List<GroupResourceDTO> list = new ArrayList<>();
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        if (CollectionUtils.isEmpty(userGroups)) {
            return permissionDTO;
        }
        permissionDTO.setUserGroups(userGroups);
        List<String> groupList = userGroups.stream().map(UserGroup::getGroupId).collect(Collectors.toList());
        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andIdIn(groupList);
        List<Group> groups = groupMapper.selectByExample(groupExample);
        permissionDTO.setGroups(groups);
        for (Group gp : groups) {
            GroupResourceDTO dto = new GroupResourceDTO();
            dto.setGroup(gp);
            UserGroupPermissionExample userGroupPermissionExample = new UserGroupPermissionExample();
            userGroupPermissionExample.createCriteria().andGroupIdEqualTo(gp.getId());
            List<UserGroupPermission> userGroupPermissions = userGroupPermissionMapper.selectByExample(userGroupPermissionExample);
            dto.setUserGroupPermissions(userGroupPermissions);
            list.add(dto);
        }
        permissionDTO.setList(list);
        return permissionDTO;
    }


    public boolean checkUserPassword(String userId, String password) {
        if (StringUtils.isBlank(userId)) {
            MSException.throwException(Translator.get("user_name_is_null"));
        }
        if (StringUtils.isBlank(password)) {
            MSException.throwException(Translator.get("password_is_null"));
        }
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andPasswordEqualTo(CodingUtil.md5(password));
        return userMapper.countByExample(example) > 0;
    }

    public User selectUser(String userId, String email) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            if (StringUtils.isNotBlank(email)) {
                UserExample example = new UserExample();
                example.createCriteria().andEmailEqualTo(email);
                List<User> users = userMapper.selectByExample(example);
                if (!CollectionUtils.isEmpty(users)) {
                    return users.get(0);
                }
            }
        }
        return user;
    }

    public void createOssUser(User user) {
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setStatus(UserStatus.NORMAL);
        if (StringUtils.isBlank(user.getEmail())) {
            user.setEmail(user.getId() + "@metershpere.io");
        }
        userMapper.insertSelective(user);
    }

    public void addLdapUser(User user) {
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setStatus(UserStatus.NORMAL);
        checkEmailIsExist(user.getEmail());
        userMapper.insertSelective(user);
    }

    private void checkEmailIsExist(String email) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andEmailEqualTo(email);
        List<User> userList = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(userList)) {
            MSException.throwException(Translator.get("user_email_already_exists"));
        }
    }

    public void updateUser(User user) {
        // todo 提取重复代码
        if (StringUtils.isNotBlank(user.getEmail())) {
            UserExample example = new UserExample();
            UserExample.Criteria criteria = example.createCriteria();
            criteria.andEmailEqualTo(user.getEmail());
            criteria.andIdNotEqualTo(user.getId());
            if (userMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("user_email_already_exists"));
            }
        }
        user.setPassword(null);
        user.setUpdateTime(System.currentTimeMillis());
        // 变更前
        User userFromDB = userMapper.selectByPrimaryKey(user.getId());
        // last workspace id 变了
        if (user.getLastWorkspaceId() != null && !StringUtils.equals(user.getLastWorkspaceId(), userFromDB.getLastWorkspaceId())) {
            List<Project> projects = getProjectListByWsAndUserId(user.getId(), user.getLastWorkspaceId());
            if (projects.size() > 0) {
                // 如果传入的 last_project_id 是 last_workspace_id 下面的
                boolean present = projects.stream().anyMatch(p -> StringUtils.equals(p.getId(), user.getLastProjectId()));
                if (!present) {
                    user.setLastProjectId(projects.get(0).getId());
                }
            } else {
                user.setLastProjectId(StringUtils.EMPTY);
            }
        }
        // 执行变更
        userMapper.updateByPrimaryKeySelective(user);
        if (StringUtils.equals(user.getStatus(), UserStatus.DISABLED)) {
            SessionUtils.kickOutUser(user.getId());
        }
    }

    private List<Project> getProjectListByWsAndUserId(String userId, String workspaceId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        List<Project> projects = projectMapper.selectByExample(projectExample);

        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<Project> projectList = new ArrayList<>();
        userGroups.forEach(userGroup -> projects.forEach(project -> {
            if (StringUtils.equals(userGroup.getSourceId(), project.getId())) {
                if (!projectList.contains(project)) {
                    projectList.add(project);
                }
            }
        }));
        return projectList;
    }
}
