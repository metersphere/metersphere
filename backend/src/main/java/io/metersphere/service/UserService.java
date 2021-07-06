package io.metersphere.service;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtOrganizationMapper;
import io.metersphere.base.mapper.ext.ExtUserGroupMapper;
import io.metersphere.base.mapper.ext.ExtUserMapper;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.controller.request.member.AddMemberRequest;
import io.metersphere.controller.request.member.EditPassWordRequest;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.controller.request.member.UserRequest;
import io.metersphere.controller.request.organization.AddOrgMemberRequest;
import io.metersphere.controller.request.organization.QueryOrgMemberRequest;
import io.metersphere.controller.request.resourcepool.UserBatchProcessRequest;
import io.metersphere.dto.*;
import io.metersphere.excel.domain.*;
import io.metersphere.excel.listener.EasyExcelListener;
import io.metersphere.excel.listener.UserDataListener;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.security.MsUserToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.commons.constants.SessionConstants.ATTR_USER;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Lazy
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private UserGroupPermissionMapper userGroupPermissionMapper;
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private ExtUserGroupMapper extUserGroupMapper;
    @Resource
    private ProjectMapper projectMapper;

    public List<UserDetail> queryTypeByIds(List<String> userIds) {
        return extUserMapper.queryTypeByIds(userIds);
    }

    public Map<String, User> queryNameByIds(List<String> userIds) {
        return extUserMapper.queryNameByIds(userIds);
    }

    public Map<String, User> queryName() {
        return extUserMapper.queryName();
    }

    public UserDTO insert(UserRequest user) {
        checkUserParam(user);
        //
        String id = user.getId();
        User user1 = userMapper.selectByPrimaryKey(id);
        if (user1 != null) {
            MSException.throwException(Translator.get("user_id_already_exists"));
        } else {
            createUser(user);
        }

        List<Map<String, Object>> groups = user.getGroups();
        if (!groups.isEmpty()) {
            insertUserGroup(groups, user.getId());
        }
        return getUserDTO(user.getId());
    }

    public void insertUserGroup(List<Map<String, Object>> groups, String userId) {
        for (Map<String, Object> map : groups) {
            String idType = (String) map.get("type");
            String[] arr = idType.split("\\+");
            String groupId = arr[0];
            String type = arr[1];
            if (StringUtils.equals(type, UserGroupType.SYSTEM)) {
                UserGroup userGroup = new UserGroup();
                userGroup.setId(UUID.randomUUID().toString());
                userGroup.setUserId(userId);
                userGroup.setGroupId(groupId);
                userGroup.setSourceId("system");
                userGroup.setCreateTime(System.currentTimeMillis());
                userGroup.setUpdateTime(System.currentTimeMillis());
                userGroupMapper.insertSelective(userGroup);
            } else {
                List<String> ids = (List<String>) map.get("ids");
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                UserGroupMapper mapper = sqlSession.getMapper(UserGroupMapper.class);
                for (String id : ids) {
                    UserGroup userGroup = new UserGroup();
                    userGroup.setId(UUID.randomUUID().toString());
                    userGroup.setUserId(userId);
                    userGroup.setGroupId(groupId);
                    userGroup.setSourceId(id);
                    userGroup.setCreateTime(System.currentTimeMillis());
                    userGroup.setUpdateTime(System.currentTimeMillis());
                    mapper.insertSelective(userGroup);
                }
                sqlSession.flushStatements();
            }

        }
    }

    public User selectUser(String userId, String email) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            UserExample example = new UserExample();
            example.createCriteria().andEmailEqualTo(email);
            List<User> users = userMapper.selectByExample(example);
            if (!CollectionUtils.isEmpty(users)) {
                return users.get(0);
            }
        }
        return user;

    }

    private void insertUserRole(List<Map<String, Object>> roles, String userId) {
        for (int i = 0; i < roles.size(); i++) {
            Map<String, Object> map = roles.get(i);
            String role = (String) map.get("id");
            if (StringUtils.equals(role, RoleConstants.ADMIN)) {
                UserRole userRole = new UserRole();
                userRole.setId(UUID.randomUUID().toString());
                userRole.setUserId(userId);
                userRole.setUpdateTime(System.currentTimeMillis());
                userRole.setCreateTime(System.currentTimeMillis());
                userRole.setRoleId(role);
                // TODO 修改
                userRole.setSourceId("adminSourceId");
                userRoleMapper.insertSelective(userRole);
            } else {
                List<String> list = (List<String>) map.get("ids");
                for (int j = 0; j < list.size(); j++) {
                    UserRole userRole1 = new UserRole();
                    userRole1.setId(UUID.randomUUID().toString());
                    userRole1.setUserId(userId);
                    userRole1.setRoleId(role);
                    userRole1.setUpdateTime(System.currentTimeMillis());
                    userRole1.setCreateTime(System.currentTimeMillis());
                    userRole1.setSourceId(list.get(j));
                    userRoleMapper.insertSelective(userRole1);
                }
            }
        }
    }

    private void checkUserParam(User user) {

        if (StringUtils.isBlank(user.getId())) {
            MSException.throwException(Translator.get("user_id_is_null"));
        }

        if (StringUtils.isBlank(user.getName())) {
            MSException.throwException(Translator.get("user_name_is_null"));
        }

        if (StringUtils.isBlank(user.getEmail())) {
            MSException.throwException(Translator.get("user_email_is_null"));
        }
        // password
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

    public void createOssUser(User user) {
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setStatus(UserStatus.NORMAL);
        if (StringUtils.isBlank(user.getEmail())) {
            user.setEmail(user.getId() + "@metershpere.io");
        }
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

    public UserRoleDTO getUserRole(String userId) {
        UserRoleDTO userRoleDTO = new UserRoleDTO();
        //
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRole> userRoleList = userRoleMapper.selectByExample(userRoleExample);

        if (CollectionUtils.isEmpty(userRoleList)) {
            return userRoleDTO;
        }
        // 设置 user_role
        userRoleDTO.setUserRoles(userRoleList);

        List<String> roleIds = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList());

        RoleExample roleExample = new RoleExample();
        roleExample.createCriteria().andIdIn(roleIds);

        List<Role> roleList = roleMapper.selectByExample(roleExample);
        userRoleDTO.setRoles(roleList);

        return userRoleDTO;
    }

    public List<User> getUserList() {
        UserExample example = new UserExample();
        example.setOrderByClause("update_time desc");
        return userMapper.selectByExample(example);
    }

    public List<User> getUserListWithRequest(io.metersphere.controller.request.UserRequest request) {
        return extUserMapper.getUserList(request);
    }

    public void deleteUser(String userId) {
        SessionUser user = SessionUtils.getUser();
        if (StringUtils.equals(user.getId(), userId)) {
            MSException.throwException(Translator.get("cannot_delete_current_user"));
        }

        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        userGroupMapper.deleteByExample(userGroupExample);

        userMapper.deleteByPrimaryKey(userId);
    }

    public void updateUserRole(UserRequest user) {
        String userId = user.getId();
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> list = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(list)) {
            if (list.contains(user.getLastWorkspaceId()) || list.contains(user.getLastOrganizationId())) {
                user.setLastOrganizationId(null);
                user.setLastWorkspaceId(null);
                userMapper.updateByPrimaryKeySelective(user);
            }
        }

        userGroupMapper.deleteByExample(userGroupExample);
        List<Map<String, Object>> groups = user.getGroups();
        if (!groups.isEmpty()) {
            insertUserGroup(groups, user.getId());
        }

        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(user.getEmail());
        criteria.andIdNotEqualTo(user.getId());
        if (userMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("user_email_already_exists"));
        }
        user.setUpdateTime(System.currentTimeMillis());
        userMapper.updateByPrimaryKeySelective(user);
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
        userMapper.updateByPrimaryKeySelective(user);
        // 禁用用户之后，剔除在线用户
        if (StringUtils.equals(user.getStatus(), UserStatus.DISABLED)) {
            SessionUtils.kickOutUser(user.getId());
        }
    }

    public void switchUserRole(String sign, String sourceId) {
        SessionUser sessionUser = SessionUtils.getUser();
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();

        if (StringUtils.equals("organization", sign)) {
            user.setLastOrganizationId(sourceId);
            List<Workspace> workspaces = workspaceService.getWorkspaceListByOrgIdAndUserId(user.getId(), sourceId);
            if (workspaces.size() > 0) {
                user.setLastWorkspaceId(workspaces.get(0).getId());
                List<Project> projects = getProjectListByWsAndUserId(workspaces.get(0).getId());
                if (projects.size() > 0) {
                    user.setLastProjectId(projects.get(0).getId());
                } else {
                    user.setLastProjectId("");
                }
            } else {
                user.setLastWorkspaceId("");
                user.setLastProjectId("");
            }
        }
        if (StringUtils.equals("workspace", sign)) {
            Workspace workspace = workspaceMapper.selectByPrimaryKey(sourceId);
            user.setLastOrganizationId(workspace.getOrganizationId());
            user.setLastWorkspaceId(sourceId);
            List<Project> projects = getProjectListByWsAndUserId(sourceId);
            if (projects.size() > 0) {
                user.setLastProjectId(projects.get(0).getId());
            } else {
                user.setLastProjectId("");
            }
        }
        BeanUtils.copyProperties(user, newUser);
        // 切换工作空间或组织之后更新 session 里的 user
        SessionUtils.putUser(SessionUser.fromUser(user));
        userMapper.updateByPrimaryKeySelective(newUser);
    }

    private List<Project> getProjectListByWsAndUserId(String workspaceId) {
        String useId = SessionUtils.getUser().getId();
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        List<Project> projects = projectMapper.selectByExample(projectExample);

        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(useId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<Project> projectList = new ArrayList<>();
        userGroups.forEach(userGroup -> {
            projects.forEach(project -> {
                if (StringUtils.equals(userGroup.getSourceId(), project.getId())) {
                    if (!projectList.contains(project)) {
                        projectList.add(project);
                    }
                }
            });
        });
        return projectList;
    }

    public UserDTO getUserInfo(String userId) {
        return getUserDTO(userId);
    }

    public List<User> getMemberList(QueryMemberRequest request) {
        return extUserGroupMapper.getMemberList(request);
    }

    public void addMember(AddMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            for (String userId : request.getUserIds()) {
                UserGroupExample userGroupExample = new UserGroupExample();
                userGroupExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(request.getWorkspaceId());
                List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
                if (userGroups.size() > 0) {
                    MSException.throwException(Translator.get("user_already_exists"));
                } else {
                    for (String groupId : request.getGroupIds()) {
                        UserGroup userGroup = new UserGroup();
                        userGroup.setGroupId(groupId);
                        userGroup.setSourceId(request.getWorkspaceId());
                        userGroup.setUserId(userId);
                        userGroup.setId(UUID.randomUUID().toString());
                        userGroup.setUpdateTime(System.currentTimeMillis());
                        userGroup.setCreateTime(System.currentTimeMillis());
                        userGroupMapper.insertSelective(userGroup);
                    }
                }
            }
        }
    }

    public void deleteMember(String workspaceId, String userId) {
        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andTypeEqualTo(UserGroupType.WORKSPACE);
        List<Group> groups = groupMapper.selectByExample(groupExample);

        List<String> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupIds)) {
            return;
        }
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId)
                .andSourceIdEqualTo(workspaceId)
                .andGroupIdIn(groupIds);
        User user = userMapper.selectByPrimaryKey(userId);
        if (StringUtils.equals(workspaceId, user.getLastWorkspaceId())) {
            user.setLastWorkspaceId("");
            user.setLastOrganizationId("");
            userMapper.updateByPrimaryKeySelective(user);
        }

        userGroupMapper.deleteByExample(userGroupExample);
    }

    public void addOrganizationMember(AddOrgMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            for (String userId : request.getUserIds()) {
                UserGroupExample userGroupExample = new UserGroupExample();
                userGroupExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(request.getOrganizationId());
                List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
                if (userGroups.size() > 0) {
                    MSException.throwException(Translator.get("user_already_exists") + ": " + userId);
                } else {
                    for (String groupId : request.getGroupIds()) {
                        UserGroup userGroup = new UserGroup();
                        userGroup.setId(UUID.randomUUID().toString());
                        userGroup.setUserId(userId);
                        userGroup.setGroupId(groupId);
                        userGroup.setSourceId(request.getOrganizationId());
                        userGroup.setCreateTime(System.currentTimeMillis());
                        userGroup.setUpdateTime(System.currentTimeMillis());
                        userGroupMapper.insertSelective(userGroup);
                    }
                }
            }
        }
    }

    public void delOrganizationMember(String organizationId, String userId) {

        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andTypeEqualTo(UserGroupType.ORGANIZATION);
        List<Group> groups = groupMapper.selectByExample(groupExample);
        List<String> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(groupIds)) {
            return;
        }

        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId)
                .andGroupIdIn(groupIds)
                .andSourceIdEqualTo(organizationId);

        User user = userMapper.selectByPrimaryKey(userId);
        if (StringUtils.equals(organizationId, user.getLastOrganizationId())) {
            user.setLastWorkspaceId("");
            user.setLastOrganizationId("");
            userMapper.updateByPrimaryKeySelective(user);
        }

        userGroupMapper.deleteByExample(userGroupExample);
    }

    public List<User> getOrgMemberList(QueryOrgMemberRequest request) {
        return extUserGroupMapper.getOrgMemberList(request);
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

    /**
     * 查询该组织外的其他用户列表
     */
    public List<User> getBesideOrgMemberList(String orgId) {
        return extUserRoleMapper.getBesideOrgMemberList(orgId);
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

    public void refreshSessionUser(String sign, String sourceId) {
        SessionUser sessionUser = SessionUtils.getUser();
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();
        if (StringUtils.equals("organization", sign) && StringUtils.equals(sourceId, user.getLastOrganizationId())) {
            user.setLastOrganizationId("");
            user.setLastWorkspaceId("");
        }
        if (StringUtils.equals("workspace", sign) && StringUtils.equals(sourceId, user.getLastWorkspaceId())) {
            user.setLastWorkspaceId("");
        }

        BeanUtils.copyProperties(user, newUser);

        SessionUtils.putUser(SessionUser.fromUser(user));
        userMapper.updateByPrimaryKeySelective(newUser);
    }


    /*修改当前用户用户密码*/
    private User updateCurrentUserPwd(EditPassWordRequest request) {
        String oldPassword = CodingUtil.md5(request.getPassword(), "utf-8");
        String newPassword = request.getNewpassword();
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
        return extUserMapper.updatePassword(user);
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
        return extUserMapper.updatePassword(user);
    }

    public String getDefaultLanguage() {
        final String key = "default.language";
        return extUserMapper.getDefaultLanguage(key);
    }

    public List<User> getTestManagerAndTestUserList(QueryMemberRequest request) {
        return extUserRoleMapper.getTestManagerAndTestUserList(request);
    }

    public ResultHolder login(LoginRequest request) {
        String login = (String) SecurityUtils.getSubject().getSession().getAttribute("authenticate");
        String username = StringUtils.trim(request.getUsername());
        String password = "";
        if (!StringUtils.equals(login, UserSource.LDAP.name())) {
            password = StringUtils.trim(request.getPassword());
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                return ResultHolder.error("user or password can't be null");
            }
        }

        MsUserToken token = new MsUserToken(username, password, login);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            if (subject.isAuthenticated()) {
                UserDTO user = (UserDTO) subject.getSession().getAttribute(ATTR_USER);
                autoSwitch(user);
                return ResultHolder.success(subject.getSession().getAttribute("user"));
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

    private void autoSwitch(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastProjectId())) {
            List<UserGroup> projectUserGroups = user.getUserGroups().stream()
                    .filter(ug -> StringUtils.equals(user.getLastProjectId(), ug.getSourceId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(projectUserGroups)) {
                return;
            }
        }
        List<UserGroup> userGroups = user.getUserGroups();
        List<String> projectGroupIds = user.getGroups()
                .stream().filter(ug -> StringUtils.equals(ug.getType(), UserGroupType.PROJECT))
                .map(Group::getId)
                .collect(Collectors.toList());
        List<UserGroup> project = userGroups.stream().filter(ug -> projectGroupIds.contains(ug.getGroupId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(project)) {
            // 项目用户组为空切换工作空间
            List<String> orgIds = user.getGroups()
                    .stream()
                    .filter(ug -> StringUtils.equals(ug.getType(), UserGroupType.ORGANIZATION))
                    .map(Group::getId)
                    .collect(Collectors.toList());
            List<String> testIds = user.getGroups()
                    .stream()
                    .filter(ug -> StringUtils.equals(ug.getType(), UserGroupType.WORKSPACE))
                    .map(Group::getId)
                    .collect(Collectors.toList());
            List<UserGroup> org = userGroups.stream().filter(ug -> orgIds.contains(ug.getGroupId()))
                    .collect(Collectors.toList());
            List<UserGroup> test = userGroups.stream().filter(ug -> testIds.contains(ug.getGroupId()))
                    .collect(Collectors.toList());
            if (test.size() > 0) {
                String wsId = test.get(0).getSourceId();
                switchUserRole("workspace", wsId);
            } else if (org.size() > 0) {
                String orgId = org.get(0).getSourceId();
                switchUserRole("organization", orgId);
            }
        } else {
            UserGroup userGroup = project.stream().filter(p -> StringUtils.isNotBlank(p.getSourceId()))
                    .collect(Collectors.toList()).get(0);
            String projectId = userGroup.getSourceId();
            Project p = projectMapper.selectByPrimaryKey(projectId);
            String wsId = p.getWorkspaceId();
            Workspace workspace = workspaceMapper.selectByPrimaryKey(wsId);
            String orgId = workspace.getOrganizationId();
            user.setId(user.getId());
            user.setLastProjectId(projectId);
            user.setLastWorkspaceId(wsId);
            user.setLastOrganizationId(orgId);
            updateUser(user);
            SessionUtils.putUser(SessionUser.fromUser(user));
        }

    }

    public List<User> searchUser(String condition) {
        return extUserMapper.searchUser(condition);
    }

    public void logout() throws Exception {
        SSOService ssoService = CommonBeanFactory.getBean(SSOService.class);
        if (ssoService != null) {
            ssoService.logout();
        }
    }

    public void userTemplateExport(HttpServletResponse response) {
        try {
            EasyExcelExporter easyExcelExporter = new EasyExcelExporter(new UserExcelDataFactory().getExcelDataByLocal());
            easyExcelExporter.export(response, generateExportTemplate(),
                    Translator.get("user_import_template_name"), Translator.get("user_import_template_sheet"));
        } catch (Exception e) {
            MSException.throwException(e);
        }
    }

    private List<UserExcelData> generateExportTemplate() {
        List<UserExcelData> list = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            UserExcelData data = new UserExcelData();
            data.setId("user_id_" + i);
            data.setName(Translator.get("user") + i);
            data.setEmail(Translator.get("required"));
            data.setUserIsAdmin(Translator.get("options_no"));
            data.setUserIsTester(Translator.get("options_no"));
            data.setUserIsOrgMember(Translator.get("options_no"));
            data.setUserIsViewer(Translator.get("options_no"));
            data.setUserIsTestManager(Translator.get("options_no"));
            data.setUserIsProjectAdmin(Translator.get("options_no"));
            data.setUserIsProjectMember(Translator.get("options_no"));
            data.setUserIsOrgAdmin(Translator.get("options_yes"));
            data.setOrgAdminOrganization("默认组织");
            list.add(data);
        }
        list.add(new UserExcelData());
        UserExcelData explain = new UserExcelData();
        explain.setId("ID不支持中文");
        explain.setName(Translator.get("do_not_modify_header_order"));
        explain.setOrgAdminOrganization("多个组织请换行展示");
        explain.setPassword(Translator.get("required") + ";" + Translator.get("password_format_is_incorrect"));
        list.add(explain);
        return list;
    }

    public ExcelResponse userImport(MultipartFile multipartFile, HttpServletRequest request) {

        ExcelResponse excelResponse = new ExcelResponse();
        List<ExcelErrData<TestCaseExcelData>> errList = null;
        if (multipartFile == null) {
            MSException.throwException(Translator.get("upload_fail"));
        }
        try {
            Class clazz = new UserExcelDataFactory().getExcelDataByLocal();

            Map<String, String> orgNameMap = new HashMap<>();
            Map<String, String> workspaceNameMap = new HashMap<>();
            Map<String, String> projectNameMap = new HashMap<>();

            List<OrganizationMemberDTO> organizationList = extOrganizationMapper.findIdAndNameByOrganizationId("All");
            for (OrganizationMemberDTO model : organizationList) {
                orgNameMap.put(model.getName(), model.getId());
            }
            List<WorkspaceDTO> workspaceList = workspaceService.findIdAndNameByOrganizationId("All");
            for (WorkspaceDTO model : workspaceList) {
                workspaceNameMap.put(model.getName(), model.getId());
            }
            List<Project> projectList = projectMapper.selectByExample(new ProjectExample());
            for (Project pro : projectList) {
                projectNameMap.put(pro.getName(), pro.getId());
            }

            EasyExcelListener easyExcelListener = new UserDataListener(clazz, workspaceNameMap, orgNameMap, projectNameMap);
            EasyExcelFactory.read(multipartFile.getInputStream(), clazz, easyExcelListener).sheet().doRead();
            if (CollectionUtils.isNotEmpty(((UserDataListener) easyExcelListener).getNames())) {
                request.setAttribute("ms-req-title", String.join(",", ((UserDataListener) easyExcelListener).getNames()));
                request.setAttribute("ms-req-source-id", JSON.toJSONString(((UserDataListener) easyExcelListener).getIds()));
            }
            errList = easyExcelListener.getErrList();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }

        //如果包含错误信息就导出错误信息
        if (!errList.isEmpty()) {
            excelResponse.setSuccess(false);
            excelResponse.setErrList(errList);
        } else {
            excelResponse.setSuccess(true);
        }
        return excelResponse;
    }

    public List<String> selectAllId() {
        return extUserMapper.selectAllId();
    }

    /**
     * 批量处理用户信息:
     * 添加用户到工作空间；
     * 添加用户权限
     *
     * @param request
     */
    public void batchProcessUserInfo(UserBatchProcessRequest request) {
        String batchType = request.getBatchType();
        if (StringUtils.equals(BatchProcessUserInfoType.ADD_PROJECT.name(), batchType)) {
            batchAddUserToProject(request);
        } else {
            batchAddUserGroup(request);
        }
    }

    private void batchAddUserGroup(UserBatchProcessRequest request) {
        List<String> userIds = this.selectIdByUserRequest(request);
        // groupId+resourceId e.g. admin+none
        List<String> groupResources = request.getBatchProcessValue();
        Map<String, List<String>> sourceMap = new HashMap<>();
        for (String groupResource : groupResources) {
            String[] split = groupResource.split("\\+");
            String groupId = split[0];
            String sourceId = split[1];
            if (sourceMap.get(groupId) == null) {
                List<String> list = new ArrayList<>();
                list.add(sourceId);
                sourceMap.put(groupId, list);
            } else {
                sourceMap.get(groupId).add(sourceId);
            }
        }

        for (String userId : userIds) {
            Set<String> set = sourceMap.keySet();
            for (String group : set) {
                Group gp = groupMapper.selectByPrimaryKey(group);
                if (gp != null) {
                    if (StringUtils.equals(UserGroupType.SYSTEM, gp.getType())) {
                        UserGroupExample userGroupExample = new UserGroupExample();
                        userGroupExample.createCriteria().andGroupIdEqualTo(group).andUserIdEqualTo(userId);
                        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
                        if (CollectionUtils.isEmpty(userGroups)) {
                            UserGroup userGroup = new UserGroup();
                            userGroup.setId(UUID.randomUUID().toString());
                            userGroup.setGroupId(group);
                            userGroup.setSourceId("system");
                            userGroup.setUserId(userId);
                            userGroup.setUpdateTime(System.currentTimeMillis());
                            userGroup.setCreateTime(System.currentTimeMillis());
                            userGroupMapper.insertSelective(userGroup);
                        }
                    } else {
                        // 组织、工作空间、项目
                        UserGroupExample userGroupExample = new UserGroupExample();
                        userGroupExample.createCriteria().andGroupIdEqualTo(group).andUserIdEqualTo(userId);
                        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
                        List<String> sourceIds = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());
                        List<String> list = sourceMap.get(group);
                        list.removeAll(sourceIds);
                        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                        UserGroupMapper mapper = sqlSession.getMapper(UserGroupMapper.class);
                        for (String sourceId : list) {
                            UserGroup userGroup = new UserGroup();
                            userGroup.setId(UUID.randomUUID().toString());
                            userGroup.setUserId(userId);
                            userGroup.setGroupId(group);
                            userGroup.setSourceId(sourceId);
                            userGroup.setCreateTime(System.currentTimeMillis());
                            userGroup.setUpdateTime(System.currentTimeMillis());
                            mapper.insertSelective(userGroup);
                        }
                        sqlSession.flushStatements();
                    }
                }
            }
        }
    }

    private void batchAddUserToProject(UserBatchProcessRequest request) {
        List<String> userIds = this.selectIdByUserRequest(request);
        String toSetGroup = UserGroupConstants.READ_ONLY;
        List<String> projectIds = request.getBatchProcessValue();
        for (String userId : userIds) {
            UserGroupExample userGroupExample = new UserGroupExample();
            userGroupExample
                    .createCriteria()
                    .andUserIdEqualTo(userId)
                    .andGroupIdEqualTo(toSetGroup);
            List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
            List<String> exist = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());
            projectIds.removeAll(exist);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            UserGroupMapper mapper = sqlSession.getMapper(UserGroupMapper.class);
            for (String projectId : projectIds) {
                UserGroup userGroup = new UserGroup();
                userGroup.setId(UUID.randomUUID().toString());
                userGroup.setUserId(userId);
                userGroup.setGroupId(toSetGroup);
                userGroup.setSourceId(projectId);
                userGroup.setCreateTime(System.currentTimeMillis());
                userGroup.setUpdateTime(System.currentTimeMillis());
                mapper.insertSelective(userGroup);
            }
            sqlSession.flushStatements();
        }
    }

    private List<String> selectIdByUserRequest(UserBatchProcessRequest request) {

        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            List<String> userIdList = new ArrayList<>();
            if (StringUtils.isEmpty(request.getOrganizationId())) {
                userIdList = extUserMapper.selectIdsByQuery(request.getCondition());
            } else {
                //组织->成员 页面发起的请求
                userIdList = extUserRoleMapper.selectIdsByQuery(request.getOrganizationId(), request.getCondition());
            }

            return userIdList;
        } else {
            return request.getIds();
        }

    }

    private Map<String, List<String>> genRoleResourceMap(List<String> batchProcessValue) {
        Map<String, List<String>> returnMap = new HashMap<>();
        Map<String, String> workspaceToOrgMap = new HashMap<>();
        for (String string : batchProcessValue) {
            String[] stringArr = string.split("<->");
            // string格式： 资源ID<->权限<->workspace/org
            if (stringArr.length == 3) {
                String resourceID = stringArr[0];
                String role = stringArr[1];
                String sourceType = stringArr[2];
                String finalResourceId = resourceID;
                if (StringUtils.equalsIgnoreCase(sourceType, "workspace")) {
                    if (StringUtils.equalsAnyIgnoreCase(role, RoleConstants.ORG_ADMIN, RoleConstants.ORG_MEMBER)) {
                        finalResourceId = workspaceToOrgMap.get(resourceID);
                        if (finalResourceId == null) {
                            finalResourceId = workspaceService.getOrganizationIdById(resourceID);
                            workspaceToOrgMap.put(resourceID, finalResourceId);
                        }
                    }
                }
                if (StringUtils.isNotEmpty(finalResourceId)) {
                    if (returnMap.containsKey(role)) {
                        if (!returnMap.get(role).contains(finalResourceId)) {
                            returnMap.get(role).add(finalResourceId);
                        }

                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(finalResourceId);
                        returnMap.put(role, list);
                    }
                }
            }
        }
        return returnMap;
    }

    private UserRequest convert2UserRequest(String userID, Map<String, List<String>> roleIdMap, List<UserRole> userRoles) {
        Map<String, List<String>> userRoleAndResourceMap = userRoles.stream().collect(
                Collectors.groupingBy(UserRole::getRoleId, Collectors.mapping(UserRole::getSourceId, Collectors.toList())));
        List<Map<String, Object>> roles = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : roleIdMap.entrySet()) {
            String role = entry.getKey();
            List<String> rawResourceIDList = entry.getValue();
            List<String> resourceIDList = new ArrayList<>();
            for (String resourceID : rawResourceIDList) {
                if (userRoleAndResourceMap.containsKey(role) && userRoleAndResourceMap.get(role).contains(resourceID)) {
                    continue;
                }
                resourceIDList.add(resourceID);
            }
            if (resourceIDList.isEmpty()) {
                continue;
            }
            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("id", role);
            roleMap.put("ids", resourceIDList);
            roles.add(roleMap);
        }
        UserRequest request = new UserRequest();
        request.setId(userID);
        request.setRoles(roles);
        return request;
    }

    public void addUserWorkspaceAndRole(UserRequest user, List<UserRole> userRoles) {
        List<Map<String, Object>> roles = user.getRoles();
        if (!roles.isEmpty()) {
            insertUserRole(roles, user.getId());
        }
        List<String> list = userRoles.stream().map(UserRole::getSourceId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(list)) {
            if (list.contains(user.getLastWorkspaceId()) || list.contains(user.getLastOrganizationId())) {
                user.setLastOrganizationId("");
                user.setLastWorkspaceId("");
                userMapper.updateByPrimaryKeySelective(user);
            }
        }
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
        // 设置 user_role
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
            nameBuilder.append(String.join(",", names)).append("\n");
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

    private String getRoles(String userId) {
        List<Map<String, Object>> maps = userRoleService.getUserGroup(userId);
        List<String> colNames = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(maps)) {
            for (Map<String, Object> map : maps) {
                String id = map.get("id").toString();
                OrganizationExample example = new OrganizationExample();
                example.createCriteria().andIdIn((List<String>) map.get("ids"));
                List<Organization> list = organizationMapper.selectByExample(example);
                List<String> names = new LinkedList<>();
                if (CollectionUtils.isNotEmpty(list)) {
                    names = list.stream().map(Organization::getName).collect(Collectors.toList());
                }
                WorkspaceExample workspaceExample = new WorkspaceExample();
                workspaceExample.createCriteria().andIdIn((List<String>) map.get("ids"));
                List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
                if (CollectionUtils.isNotEmpty(workspaces)) {
                    names = workspaces.stream().map(Workspace::getName).collect(Collectors.toList());
                }
                ProjectExample projectExample = new ProjectExample();
                projectExample.createCriteria().andIdIn((List<String>) map.get("ids"));
                List<Project> projects = projectMapper.selectByExample(projectExample);
                if (CollectionUtils.isNotEmpty(projects)) {
                    names = projects.stream().map(Project::getName).collect(Collectors.toList());
                }
                StringBuilder nameBuff = new StringBuilder();
                Group group = groupMapper.selectByPrimaryKey(id);
                if (group != null && CollectionUtils.isNotEmpty(names)) {
                    nameBuff.append(group.getName()).append(names);
                }
                colNames.add(nameBuff.toString());
            }
        }
        return String.join(",", colNames);
    }

    public String getLogDetails(UserBatchProcessRequest request) {
        List<String> userIdList = this.selectIdByUserRequest(request);
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIdList);
        List<User> users = userMapper.selectByExample(example);
        if (users != null) {
            List<String> names = users.stream().map(User::getName).collect(Collectors.toList());
            String roles = "\n" + "成员角色：\n" + this.getRoles(users.get(0).getId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(userIdList), null, String.join(",", names) + roles, null, new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(UserRequest request) {
        User user = userMapper.selectByPrimaryKey(request.getId());
        if (user != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(user, SystemReference.userColumns);
            for (DetailColumn column : columns) {
                if (column.getColumnName().equals("status") && column.getOriginalValue() != null && StringUtils.isNotEmpty(column.getOriginalValue().toString())) {
                    if (column.getOriginalValue().equals("0")) {
                        column.setOriginalValue("未激活");
                    } else {
                        column.setOriginalValue("已激活");
                    }
                }
            }
            DetailColumn detailColumn = new DetailColumn();
            detailColumn.setId(UUID.randomUUID().toString());
            detailColumn.setColumnTitle("角色权限");
            detailColumn.setColumnName("roles");
            detailColumn.setOriginalValue(this.getRoles(request.getId()));
            columns.add(detailColumn);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(user.getId()), null, user.getName(), user.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public List<User> getProjectMemberList(QueryMemberRequest request) {
        return extUserGroupMapper.getProjectMemberList(request);
    }

    public void addProjectMember(AddMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            for (String userId : request.getUserIds()) {
                UserGroupExample userGroupExample = new UserGroupExample();
                userGroupExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(request.getProjectId());
                List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
                if (userGroups.size() > 0) {
                    MSException.throwException(Translator.get("user_already_exists"));
                } else {
                    for (String groupId : request.getGroupIds()) {
                        UserGroup userGroup = new UserGroup();
                        userGroup.setGroupId(groupId);
                        userGroup.setSourceId(request.getProjectId());
                        userGroup.setUserId(userId);
                        userGroup.setId(UUID.randomUUID().toString());
                        userGroup.setUpdateTime(System.currentTimeMillis());
                        userGroup.setCreateTime(System.currentTimeMillis());
                        userGroupMapper.insertSelective(userGroup);
                    }
                }
            }
        }
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
            user.setLastProjectId("");
            userMapper.updateByPrimaryKeySelective(user);
        }

        userGroupMapper.deleteByExample(userGroupExample);
    }

    public List<User> getProjectMember(QueryMemberRequest request) {
        String projectId = request.getProjectId();
        if (StringUtils.isBlank(projectId)) {
            return new ArrayList<>();
        }
        return extUserGroupMapper.getProjectMemberList(request);
    }

    public List<User> getOrgAllMember(QueryOrgMemberRequest request) {
        String orgId = request.getOrganizationId();
        if (StringUtils.isBlank(orgId)) {
            return new ArrayList<>();
        }
        List<String> sourceIds = new ArrayList<>();
        sourceIds.add(orgId);

        WorkspaceExample workspaceExample = new WorkspaceExample();
        workspaceExample.createCriteria().andOrganizationIdEqualTo(orgId);
        List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
        if (CollectionUtils.isNotEmpty(workspaces)) {
            List<String> wsIds = workspaces.stream().map(Workspace::getId).collect(Collectors.toList());
            sourceIds.addAll(wsIds);
            ProjectExample projectExample = new ProjectExample();
            projectExample.createCriteria().andWorkspaceIdIn(wsIds);
            List<Project> projectList = projectMapper.selectByExample(projectExample);
            if (CollectionUtils.isNotEmpty(projectList)) {
                List<String> proIds = projectList.stream().map(Project::getId).collect(Collectors.toList());
                sourceIds.addAll(proIds);
            }
        }

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


    public void updateImportUserGroup(UserRequest user) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(user.getId());
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> list = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(list)) {
            if (list.contains(user.getLastWorkspaceId()) || list.contains(user.getLastOrganizationId())) {
                user.setLastOrganizationId("");
                user.setLastWorkspaceId("");
                userMapper.updateByPrimaryKeySelective(user);
            }
        }

        userGroupMapper.deleteByExample(userGroupExample);
        List<Map<String, Object>> groups = user.getGroups();
        saveImportUserGroup(groups, user.getId());

        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(user.getEmail());
        criteria.andIdNotEqualTo(user.getId());
        if (userMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("user_email_already_exists"));
        }
        user.setUpdateTime(System.currentTimeMillis());
        user.setPassword(null);
        userMapper.updateByPrimaryKeySelective(user);
    }

    public void saveImportUser(UserRequest userRequest) {
        checkUserParam(userRequest);
        String userId = userRequest.getId();
        User user = userMapper.selectByPrimaryKey(userId);
        if (user != null) {
            MSException.throwException(Translator.get("user_id_already_exists"));
        } else {
            createUser(userRequest);
        }
        List<Map<String, Object>> groups = userRequest.getGroups();
        saveImportUserGroup(groups, userId);
    }

    private void saveImportUserGroup(List<Map<String, Object>> groups, String userId) {
        if (!groups.isEmpty()) {
            for (Map<String, Object> map : groups) {
                String groupId = (String) map.get("id");
                if (StringUtils.equals(groupId, UserGroupConstants.ADMIN)) {
                    UserGroup userGroup = new UserGroup();
                    userGroup.setId(UUID.randomUUID().toString());
                    userGroup.setUserId(userId);
                    userGroup.setGroupId(groupId);
                    userGroup.setSourceId("system");
                    userGroup.setCreateTime(System.currentTimeMillis());
                    userGroup.setUpdateTime(System.currentTimeMillis());
                    userGroupMapper.insertSelective(userGroup);
                } else {
                    List<String> ids = (List<String>) map.get("ids");
                    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                    UserGroupMapper mapper = sqlSession.getMapper(UserGroupMapper.class);
                    for (String id : ids) {
                        UserGroup userGroup = new UserGroup();
                        userGroup.setId(UUID.randomUUID().toString());
                        userGroup.setUserId(userId);
                        userGroup.setGroupId(groupId);
                        userGroup.setSourceId(id);
                        userGroup.setCreateTime(System.currentTimeMillis());
                        userGroup.setUpdateTime(System.currentTimeMillis());
                        mapper.insertSelective(userGroup);
                    }
                    sqlSession.flushStatements();
                }
            }
        }
    }

    public UserDTO updateCurrentUser(User user) {
        String currentUserId = SessionUtils.getUserId();
        if (!StringUtils.equals(currentUserId, user.getId())) {
            MSException.throwException(Translator.get("not_authorized"));
        }
        updateUser(user);
        UserDTO userDTO = getUserDTO(user.getId());
        SessionUtils.putUser(SessionUser.fromUser(userDTO));
        return SessionUtils.getUser();
    }

    public UserDTO.PlatformInfo getCurrentPlatformInfo(String orgId) {
        User user = userMapper.selectByPrimaryKey(SessionUtils.getUserId());
        String platformInfoStr = user.getPlatformInfo();
        if (StringUtils.isBlank(orgId) || StringUtils.isBlank(platformInfoStr)) {
           return null;
        }
        JSONObject platformInfos = JSONObject.parseObject(platformInfoStr);
        JSONObject platformInfo = platformInfos.getJSONObject(orgId);
        if (platformInfo == null) {
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(platformInfo), UserDTO.PlatformInfo.class);
    }
}
