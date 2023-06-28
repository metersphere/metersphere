package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseProjectMapper;
import io.metersphere.base.mapper.ext.BaseUserGroupMapper;
import io.metersphere.base.mapper.ext.BaseUserMapper;
import io.metersphere.base.mapper.ext.BaseWorkspaceMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.handler.ResultHolder;
import io.metersphere.dto.GroupResourceDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserGroupPermissionDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.request.AuthUserIssueRequest;
import io.metersphere.request.LoginRequest;
import io.metersphere.request.member.EditPassWordRequest;
import io.metersphere.request.member.EditSeleniumServerRequest;
import io.metersphere.request.member.QueryMemberRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseUserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private BaseUserMapper baseUserMapper;

    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private UserGroupPermissionMapper userGroupPermissionMapper;
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private BaseUserGroupMapper baseUserGroupMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    private BaseWorkspaceMapper baseWorkspaceMapper;
    @Resource
    private MicroService microService;

    public Map<String, User> queryNameByIds(List<String> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>(0);
        }
        return baseUserMapper.queryNameByIds(userIds);
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

    public void createUser(User userRequest) {
        User user = new User();
        BeanUtils.copyProperties(userRequest, user);
        user.setCreateTime(System.currentTimeMillis());
        user.setCreateUser(SessionUtils.getUserId());
        user.setUpdateTime(System.currentTimeMillis());
        // 默认1:启用状态
        user.setStatus(UserStatus.NORMAL);
        user.setSource(UserSource.LOCAL.name());
        // 密码使用 MD5
        user.setPassword(CodingUtil.md5(user.getPassword()));
        checkEmailIsExist(user.getEmail());
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

    public UserDTO getUserDTO(String userId) {

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return null;
        }
        if (StringUtils.equals(user.getStatus(), UserStatus.DISABLED)) {
            throw new DisabledAccountException();
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

    public List<User> getUserList() {
        UserExample example = new UserExample();
        example.setOrderByClause("update_time desc");
        return userMapper.selectByExample(example);
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

    public void switchUserResource(String sign, String sourceId, UserDTO sessionUser) {
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();
        boolean isSuper = baseUserMapper.isSuperUser(sessionUser.getId());
        if (StringUtils.equals("workspace", sign)) {
            user.setLastWorkspaceId(sourceId);
            sessionUser.setLastWorkspaceId(sourceId);
            List<Project> projects = getProjectListByWsAndUserId(sessionUser.getId(), sourceId);
            if (CollectionUtils.isNotEmpty(projects)) {
                user.setLastProjectId(projects.get(0).getId());
            } else {
                if (isSuper) {
                    ProjectExample example = new ProjectExample();
                    example.createCriteria().andWorkspaceIdEqualTo(sourceId);
                    List<Project> allWsProject = projectMapper.selectByExample(example);
                    if (CollectionUtils.isNotEmpty(allWsProject)) {
                        user.setLastProjectId(allWsProject.get(0).getId());
                    }
                } else {
                    user.setLastProjectId(StringUtils.EMPTY);
                }
            }
        }
        BeanUtils.copyProperties(user, newUser);
        // 切换工作空间或组织之后更新 session 里的 user
        SessionUtils.putUser(SessionUser.fromUser(user, SessionUtils.getSessionId()));
        userMapper.updateByPrimaryKeySelective(newUser);
    }

    private void switchSuperUserResource(String projectId, String workspaceId, UserDTO sessionUser) {
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();
        user.setLastWorkspaceId(workspaceId);
        sessionUser.setLastWorkspaceId(workspaceId);
        user.setLastProjectId(projectId);
        BeanUtils.copyProperties(user, newUser);
        // 切换工作空间或组织之后更新 session 里的 user
        SessionUtils.putUser(SessionUser.fromUser(user, SessionUtils.getSessionId()));
        userMapper.updateByPrimaryKeySelective(newUser);
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

    public void setLanguage(String lang) {
        if (SessionUtils.getUser() != null) {
            User user = new User();
            user.setId(SessionUtils.getUser().getId());
            user.setLanguage(lang);
            updateUser(user);
            SessionUtils.getUser().setLanguage(lang);
        }
    }

    /*修改当前用户用户密码*/
    private User updateCurrentUserPwd(EditPassWordRequest request) {
        String oldPassword = CodingUtil.md5(request.getPassword(), StandardCharsets.UTF_8.name());
        String newPassword = request.getNewpassword();
        String newPasswordMd5 = CodingUtil.md5(newPassword);
        if (StringUtils.equals(oldPassword, newPasswordMd5)) {
            return null;
        }
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdEqualTo(SessionUtils.getUser().getId()).andPasswordEqualTo(oldPassword);
        List<User> users = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(users)) {
            User user = users.get(0);
            user.setPassword(CodingUtil.md5(newPassword));
            user.setUpdateTime(System.currentTimeMillis());
            return user;
        }
        MSException.throwException(Translator.get("password_modification_failed"));
        return null;
    }

    public int updateCurrentUserPassword(EditPassWordRequest request) {
        User user = updateCurrentUserPwd(request);
        return baseUserMapper.updatePassword(user);
    }

    /*管理员修改用户密码*/
    private User updateUserPwd(EditPassWordRequest request) {
        User user = userMapper.selectByPrimaryKey(request.getId());
        String newPassword = request.getNewpassword();
        user.setPassword(CodingUtil.md5(newPassword));
        user.setUpdateTime(System.currentTimeMillis());
        return user;
    }

    public int updateUserPassword(EditPassWordRequest request) {
        User user = updateUserPwd(request);
        return baseUserMapper.updatePassword(user);
    }

    public String getDefaultLanguage() {
        final String key = "default.language";
        return baseUserMapper.getDefaultLanguage(key);
    }

    public ResultHolder login(LoginRequest request) {
        String login = (String) SecurityUtils.getSubject().getSession().getAttribute("authenticate");
        String username = StringUtils.trim(request.getUsername());
        String password = StringUtils.EMPTY;
        if (!StringUtils.equals(login, UserSource.LDAP.name())) {
            password = StringUtils.trim(request.getPassword());
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                return ResultHolder.error("user or password can't be null");
            }
        }

        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            if (subject.isAuthenticated()) {
                UserDTO user = (UserDTO) subject.getSession().getAttribute(SessionConstants.ATTR_USER);
                autoSwitch(user);
                // 放入session中
                SessionUser sessionUser = SessionUser.fromUser(user, SessionUtils.getSessionId());
                SessionUtils.putUser(sessionUser);
                return ResultHolder.success(sessionUser);
            } else {
                return ResultHolder.error(Translator.get("login_fail"));
            }
        } catch (ExcessiveAttemptsException e) {
            throw new ExcessiveAttemptsException(Translator.get("excessive_attempts"));
        } catch (LockedAccountException e) {
            throw new LockedAccountException(Translator.get("user_locked"));
        } catch (DisabledAccountException e) {
            throw new DisabledAccountException(Translator.get("user_has_been_disabled"));
        } catch (ExpiredCredentialsException e) {
            throw new ExpiredCredentialsException(Translator.get("user_expires"));
        } catch (AuthenticationException e) {
            throw new AuthenticationException(e.getMessage());
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(Translator.get("not_authorized") + e.getMessage());
        }
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
                List<String> superGroupIds = user.getGroups()
                        .stream()
                        .map(Group::getId)
                        .filter(id -> StringUtils.equals(id, UserGroupConstants.SUPER_GROUP))
                        .collect(Collectors.toList());
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(superGroupIds)) {
                    Project p = baseProjectMapper.selectOne();
                    if (p != null) {
                        switchSuperUserResource(p.getId(), p.getWorkspaceId(), user);
                    }
                } else {
                    // 用户登录之后没有项目和工作空间的权限就把值清空
                    user.setLastWorkspaceId(StringUtils.EMPTY);
                    user.setLastProjectId(StringUtils.EMPTY);
                    updateUser(user);
                }
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
            } else {
                return baseUserMapper.isSuperUser(user.getId());
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
            } else {
                return baseUserMapper.isSuperUser(user.getId());
            }
        }
        return false;
    }

    public List<User> searchUser(String condition) {
        return baseUserMapper.searchUser(condition);
    }


    public List<String> selectAllId() {
        return baseUserMapper.selectAllId();
    }


    public Set<String> getUserPermission(String userId) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> groupId = userGroups.stream().map(UserGroup::getGroupId).collect(Collectors.toList());
        UserGroupPermissionExample userGroupPermissionExample = new UserGroupPermissionExample();
        userGroupPermissionExample.createCriteria().andGroupIdIn(groupId);
        List<UserGroupPermission> userGroupPermissions = userGroupPermissionMapper.selectByExample(userGroupPermissionExample);
        return userGroupPermissions.stream().map(UserGroupPermission::getPermissionId).collect(Collectors.toSet());
    }

    public UserGroupPermissionDTO getUserGroup(String userId) {
        UserGroupPermissionDTO userGroupPermissionDTO = new UserGroupPermissionDTO();
        //
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);

        if (CollectionUtils.isEmpty(userGroups)) {
            return userGroupPermissionDTO;
        }
        userGroupPermissionDTO.setUserGroups(userGroups);

        List<String> groupId = userGroups.stream().map(UserGroup::getGroupId).collect(Collectors.toList());

        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andIdIn(groupId);
        List<Group> groups = groupMapper.selectByExample(groupExample);
        userGroupPermissionDTO.setGroups(groups);
        return userGroupPermissionDTO;
    }

    public String getLogDetails(String id) {
        User user = userMapper.selectByPrimaryKey(id);
        if (user != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(user, SystemReference.userColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(user.getId()), null, user.getName(), user.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids, String id) {
        if (!CollectionUtils.isEmpty(ids)) {
            UserExample example = new UserExample();
            example.createCriteria().andIdIn(ids);
            List<User> users = userMapper.selectByExample(example);
            List<String> names = users.stream().map(User::getName).collect(Collectors.toList());

            StringBuilder nameBuilder = new StringBuilder();
            nameBuilder.append(String.join(",", names)).append(StringUtils.LF);
            for (String userId : ids) {
                UserGroupExample userGroupExample = new UserGroupExample();
                userGroupExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(id);
                List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
                if (CollectionUtils.isNotEmpty(userGroups)) {
                    List<String> groupIds = userGroups.stream().map(UserGroup::getGroupId).collect(Collectors.toList());
                    GroupExample groupExample = new GroupExample();
                    groupExample.createCriteria().andIdIn(groupIds);
                    List<Group> groups = groupMapper.selectByExample(groupExample);
                    if (CollectionUtils.isNotEmpty(groups)) {
                        List<String> strings = groups.stream().map(Group::getName).collect(Collectors.toList());
                        nameBuilder.append("用户组：").append(String.join(",", strings));
                    }
                }
            }
            List<DetailColumn> columns = new LinkedList<>();

            DetailColumn detailColumn = new DetailColumn();
            detailColumn.setId(UUID.randomUUID().toString());
            detailColumn.setColumnTitle("成员");
            detailColumn.setColumnName("roles");
            detailColumn.setOriginalValue(nameBuilder.toString());
            columns.add(detailColumn);

            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), null, nameBuilder.toString(), null, columns);
            return JSON.toJSONString(details);

        }
        return null;
    }


    public List<User> getProjectMemberList(QueryMemberRequest request) {
        return baseUserGroupMapper.getProjectMemberList(request);
    }

    public void deleteProjectMember(String projectId, String userId) {
        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andTypeEqualTo(UserGroupType.PROJECT);
        List<Group> groups = groupMapper.selectByExample(groupExample);

        List<String> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupIds)) {
            return;
        }
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId)
                .andSourceIdEqualTo(projectId)
                .andGroupIdIn(groupIds);
        User user = userMapper.selectByPrimaryKey(userId);
        if (StringUtils.equals(projectId, user.getLastProjectId())) {
            user.setLastProjectId(StringUtils.EMPTY);
            userMapper.updateByPrimaryKeySelective(user);
        }

        userGroupMapper.deleteByExample(userGroupExample);
    }

    public List<User> getWsAllMember(String workspaceId) {
        List<String> sourceIds = new ArrayList<>();
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        List<Project> projectList = projectMapper.selectByExample(projectExample);
        if (CollectionUtils.isEmpty(projectList)) {
            return new ArrayList<>();
        }

        List<String> proIds = projectList.stream().map(Project::getId).collect(Collectors.toList());
        sourceIds.addAll(proIds);
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andSourceIdIn(sourceIds);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> userIds = userGroups.stream().map(UserGroup::getUserId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }

        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        return userMapper.selectByExample(userExample);
    }


    public long getUserSize() {
        return userMapper.countByExample(new UserExample());
    }


    /**
     * 根据userId 获取 user 所属工作空间和所属工作项目
     *
     * @param userId
     */
    public Map<Object, Object> getWSAndProjectByUserId(String userId) {
        Map<Object, Object> map = new HashMap<>(2);
        List<Project> projects = baseProjectMapper.getProjectByUserId(userId);
        List<Workspace> workspaces = baseWorkspaceMapper.getWorkspaceByUserId(userId);
        map.put("project", projects);
        map.put("workspace", workspaces);
        return map;
    }

    public boolean checkWhetherChangePasswordOrNot(LoginRequest request) {
        // 升级之后 admin 还使用弱密码也提示修改
        if (StringUtils.equals("admin", request.getUsername())) {
            UserExample example = new UserExample();
            example.createCriteria().andIdEqualTo("admin")
                    .andPasswordEqualTo(CodingUtil.md5("metersphere"));
            return userMapper.countByExample(example) > 0;
        }

        return false;
    }

    public List<User> getMemberList(QueryMemberRequest request) {
        return baseUserGroupMapper.getMemberList(request);
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

    public UserDTO updateCurrentUser(User user) {
        String currentUserId = SessionUtils.getUserId();
        if (!StringUtils.equals(currentUserId, user.getId())) {
            MSException.throwException(Translator.get("not_authorized"));
        }
        updateUser(user);
        UserDTO userDTO = getUserDTO(user.getId());
        SessionUtils.putUser(SessionUser.fromUser(userDTO, SessionUtils.getSessionId()));
        return SessionUtils.getUser();
    }

    public List<User> getProjectMemberOption(String projectId) {
        return baseUserGroupMapper.getProjectMemberOption(projectId);
    }


    public int updateUserSeleniumServer(EditSeleniumServerRequest request) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdEqualTo(SessionUtils.getUser().getId());
        List<User> users = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(users)) {
            User user = users.get(0);
            String seleniumServer = request.getSeleniumServer();
            user.setSeleniumServer(StringUtils.isBlank(seleniumServer) ? StringUtils.EMPTY : seleniumServer.trim());
            user.setUpdateTime(System.currentTimeMillis());
            //更新session seleniumServer 信息
            SessionUser sessionUser = SessionUtils.getUser();
            sessionUser.setSeleniumServer(seleniumServer);
            SessionUtils.putUser(sessionUser);
            return userMapper.updateByPrimaryKeySelective(user);
        }
        MSException.throwException("更新selenium-server地址失败！");
        return 0;
    }

    public void userIssueAuth(AuthUserIssueRequest authUserIssueRequest) {
        microService.postForResultHolder(MicroServiceName.TEST_TRACK, "/issues/user/auth", authUserIssueRequest);
    }

    public void updateCurrentUserByResourceId(String resourceId) {
        Project project = baseProjectMapper.selectProjectByResourceId(resourceId);
        if (project == null) {
            MSException.throwException(Translator.get("select_resource_error_and_check"));
        }
        SessionUser user = SessionUtils.getUser();
        user.setLastProjectId(project.getId());
        user.setLastWorkspaceId(project.getWorkspaceId());
        userMapper.updateByPrimaryKeySelective(user);
    }

    public boolean isSuperUser(String userid) {
        if (StringUtils.isBlank(userid)) {
            MSException.throwException("userid is blank.");
        }
        return baseUserMapper.isSuperUser(userid);
    }

    public List<String> getAllUserIds() {
        List<User> users = userMapper.selectByExample(new UserExample());
        return users.stream().map(User::getId).collect(Collectors.toList());
    }

    public void checkUserAndProject(String userId, String projectId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            MSException.throwException(Translator.get("user_not_exist") + userId);
        }
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(projectId);
        if (userGroupMapper.countByExample(userGroupExample) == 0) {
            //检查是否是超级管理员
            userGroupExample.clear();
            userGroupExample.createCriteria().andUserIdEqualTo(userId).andGroupIdEqualTo(UserGroupConstants.SUPER_GROUP);
            if (userGroupMapper.countByExample(userGroupExample) == 0) {
                MSException.throwException(Translator.get("user_not_exists") + userId);
            }
        }
    }
}
