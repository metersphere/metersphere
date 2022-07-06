package io.metersphere.service;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.controller.request.WorkspaceRequest;
import io.metersphere.controller.request.member.*;
import io.metersphere.controller.request.resourcepool.UserBatchProcessRequest;
import io.metersphere.dto.GroupResourceDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserGroupPermissionDTO;
import io.metersphere.dto.WorkspaceDTO;
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
import okhttp3.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.metersphere.commons.constants.SessionConstants.ATTR_USER;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Lazy
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private UserGroupPermissionMapper userGroupPermissionMapper;
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserGroupService userGroupService;
    @Resource
    private ExtUserGroupMapper extUserGroupMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtProjectMapper extProjectMapper;
    @Resource
    private ExtWorkspaceMapper extWorkspaceMapper;
    @Resource
    private ExtOperatingLogMapper extOperatingLogMapper;

    public List<UserDetail> queryTypeByIds(List<String> userIds) {
        return extUserMapper.queryTypeByIds(userIds);
    }

    public Map<String, User> queryNameByIds(List<String> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>(0);
        }
        return extUserMapper.queryNameByIds(userIds);
    }

    public Map<String, User> queryName() {
        return extUserMapper.queryName();
    }

    public UserDTO insert(UserRequest userRequest) {
        checkUserParam(userRequest);
        String id = userRequest.getId();
        User user = userMapper.selectByPrimaryKey(id);
        if (user != null) {
            MSException.throwException(Translator.get("user_id_already_exists"));
        } else {
            createUser(userRequest);
        }
        List<Map<String, Object>> groups = userRequest.getGroups();
        if (!groups.isEmpty()) {
            insertUserGroup(groups, userRequest.getId());
        }
        return getUserDTO(userRequest.getId());
    }

    public void insertUserGroup(List<Map<String, Object>> groups, String userId) {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
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
                Group group = groupMapper.selectByPrimaryKey(groupId);
                checkQuota(quotaService, group.getType(), ids, 1);
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
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }

        }
    }

    private void checkQuota(QuotaService quotaService, String type, List<String> sourceIds, int size) {
        if (quotaService != null) {
            Map<String, Integer> addMemberMap = sourceIds.stream().collect(Collectors.toMap(id -> id, id -> size));
            quotaService.checkMemberCount(addMemberMap, type);
        }
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
            if (list.contains(user.getLastWorkspaceId())) {
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
                user.setLastProjectId("");
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
        SessionUtils.putUser(SessionUser.fromUser(user));
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

    public UserDTO getUserInfo(String userId) {
        return getUserDTO(userId);
    }

    public List<User> getMemberList(QueryMemberRequest request) {
        return extUserGroupMapper.getMemberList(request);
    }

    public void addMember(AddMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
            if (CollectionUtils.isNotEmpty(request.getUserIds())) {
                checkQuota(quotaService, "WORKSPACE", Collections.singletonList(request.getWorkspaceId()), request.getUserIds().size());
            }
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
            userMapper.updateByPrimaryKeySelective(user);
        }

        userGroupMapper.deleteByExample(userGroupExample);
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

    public void refreshSessionUser(String sign, String sourceId) {
        SessionUser sessionUser = SessionUtils.getUser();
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();
        if (StringUtils.equals("organization", sign)) {
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
                // 放入session中
                SessionUser sessionUser = SessionUser.fromUser(user);
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
            data.setUserIsViewer(Translator.get("options_no"));
            data.setUserIsTestManager(Translator.get("options_no"));
            data.setUserIsProjectAdmin(Translator.get("options_no"));
            data.setUserIsProjectMember(Translator.get("options_no"));
            list.add(data);
        }
        list.add(new UserExcelData());
        UserExcelData explain = new UserExcelData();
        explain.setId("ID不支持中文");
        explain.setName(Translator.get("do_not_modify_header_order"));
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

            Map<String, String> workspaceNameMap = new HashMap<>();
            Map<String, String> projectNameMap = new HashMap<>();

            List<WorkspaceDTO> workspaceList = workspaceService.getAllWorkspaceList(new WorkspaceRequest());
            for (WorkspaceDTO model : workspaceList) {
                workspaceNameMap.put(model.getName(), model.getId());
            }
            List<Project> projectList = projectMapper.selectByExample(new ProjectExample());
            for (Project pro : projectList) {
                projectNameMap.put(pro.getName(), pro.getId());
            }

            EasyExcelListener easyExcelListener = new UserDataListener(clazz, workspaceNameMap, projectNameMap);
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
        } else if (StringUtils.equals(BatchProcessUserInfoType.ADD_WORKSPACE.name(), batchType)) {
            batchAddUserToWorkspace(request);
        } else {
            batchAddUserGroup(request);
        }
    }

    private void batchAddUserToWorkspace(UserBatchProcessRequest request) {
        List<String> userIds = this.selectIdByUserRequest(request);
        String toSetGroup = request.getSelectUserGroupId();
        if (StringUtils.isBlank(toSetGroup)) {
            MSException.throwException("batch add user to workspace error. group id is illegal");
        } else {
            // 验证用户组ID有效性
            GroupExample groupExample = new GroupExample();
            groupExample.createCriteria()
                    .andIdEqualTo(toSetGroup)
                    .andTypeEqualTo(UserGroupType.WORKSPACE);
            List<Group> groups = groupMapper.selectByExample(groupExample);
            if (CollectionUtils.isEmpty(groups)) {
                MSException.throwException("batch add user to workspace error. group id is illegal");
            }
        }

        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        List<String> worksapceIds = request.getBatchProcessValue();
        if (CollectionUtils.isNotEmpty(userIds)) {
            checkQuota(quotaService, "WORKSPACE", worksapceIds, userIds.size());
        }
        for (String userId : userIds) {
            UserGroupExample userGroupExample = new UserGroupExample();
            userGroupExample
                    .createCriteria()
                    .andUserIdEqualTo(userId)
                    .andGroupIdEqualTo(toSetGroup);
            List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
            List<String> exist = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());
            worksapceIds.removeAll(exist);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            UserGroupMapper mapper = sqlSession.getMapper(UserGroupMapper.class);
            for (String workspaceId : worksapceIds) {
                UserGroup userGroup = new UserGroup();
                userGroup.setId(UUID.randomUUID().toString());
                userGroup.setUserId(userId);
                userGroup.setGroupId(toSetGroup);
                userGroup.setSourceId(workspaceId);
                userGroup.setCreateTime(System.currentTimeMillis());
                userGroup.setUpdateTime(System.currentTimeMillis());
                mapper.insertSelective(userGroup);
            }
            sqlSession.flushStatements();
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
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
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
                        checkQuota(quotaService, gp.getType(), list, 1);
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
                        if (sqlSession != null && sqlSessionFactory != null) {
                            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                        }
                    }
                }
            }
        }
    }

    private void batchAddUserToProject(UserBatchProcessRequest request) {
        List<String> userIds = this.selectIdByUserRequest(request);
        String defaultGroup = UserGroupConstants.READ_ONLY;
        String toSetGroup = request.getSelectUserGroupId();
        if (StringUtils.isBlank(toSetGroup)) {
            toSetGroup = defaultGroup;
        } else {
            // 验证用户组ID有效性
            GroupExample groupExample = new GroupExample();
            groupExample.createCriteria()
                    .andIdEqualTo(toSetGroup)
                    .andTypeEqualTo(UserGroupType.PROJECT);
            List<Group> groups = groupMapper.selectByExample(groupExample);
            if (CollectionUtils.isEmpty(groups)) {
                toSetGroup = defaultGroup;
            }
        }

        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        List<String> projectIds = request.getBatchProcessValue();
        if (CollectionUtils.isNotEmpty(userIds)) {
            checkQuota(quotaService, "PROJECT", projectIds, userIds.size());
        }
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
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private List<String> selectIdByUserRequest(UserBatchProcessRequest request) {

        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            List<String> userIdList = new ArrayList<>();
            if (StringUtils.isEmpty(request.getWorkspaceId())) {
                userIdList = extUserMapper.selectIdsByQuery(request.getCondition());
            }

            return userIdList;
        } else {
            return request.getIds();
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
        List<Map<String, Object>> maps = userGroupService.getUserGroup(userId);
        List<String> colNames = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(maps)) {
            for (Map<String, Object> map : maps) {
                String id = map.get("id").toString();
                List<String> names = new LinkedList<>();
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
        if (CollectionUtils.isEmpty(request.getUserIds())) {
            LogUtil.info("add project member warning, request param user id list empty!");
            return;
        }
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (CollectionUtils.isNotEmpty(request.getUserIds())) {
            checkQuota(quotaService, "PROJECT", Collections.singletonList(request.getProjectId()), request.getUserIds().size());
        }
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


    public void updateImportUserGroup(UserRequest user) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(user.getId());
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> list = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(list)) {
            if (list.contains(user.getLastWorkspaceId())) {
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
            QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
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
                    Group group = groupMapper.selectByPrimaryKey(groupId);
                    checkQuota(quotaService, group.getType(), ids, 1);
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
                    if (sqlSession != null && sqlSessionFactory != null) {
                        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                    }
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

    public UserDTO.PlatformInfo getCurrentPlatformInfo(String workspaceId) {
        User user = userMapper.selectByPrimaryKey(SessionUtils.getUserId());
        String platformInfoStr = user.getPlatformInfo();
        if (StringUtils.isBlank(workspaceId) || StringUtils.isBlank(platformInfoStr) || platformInfoStr.equals("null")) {
            return null;
        }
        JSONObject platformInfos = JSONObject.parseObject(platformInfoStr);
        JSONObject platformInfo = platformInfos.getJSONObject(workspaceId);
        if (platformInfo == null) {
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(platformInfo), UserDTO.PlatformInfo.class);
    }

    public void updateCurrentUserByResourceId(String resourceId) {
        Project project = extProjectMapper.selectProjectByResourceId(resourceId);
        if (project == null) {
            return;
        }
        SessionUser user = SessionUtils.getUser();
        user.setLastProjectId(project.getId());
        user.setLastWorkspaceId(project.getWorkspaceId());
        userMapper.updateByPrimaryKeySelective(user);
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
        List<Project> projects = extProjectMapper.getProjectByUserId(userId);
        List<Workspace> workspaces = extWorkspaceMapper.getWorkspaceByUserId(userId);
        map.put("project", projects);
        map.put("workspace", workspaces);
        return map;
    }

    public boolean checkWhetherChangePasswordOrNot(LoginRequest request) {
        // 只验证admin
        // 升级之后 admin 还使用弱密码也提示修改
        if (StringUtils.equals("admin", request.getUsername())) {
            UserExample example = new UserExample();
            example.createCriteria().andIdEqualTo("admin")
                    .andPasswordEqualTo(CodingUtil.md5("metersphere"));
            return userMapper.countByExample(example) > 0;
        }

        return false;
    }

    public int updateUserSeleniumServer(EditSeleniumServerRequest request) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdEqualTo(SessionUtils.getUser().getId());
        List<User> users = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(users)) {
            User user = users.get(0);
            String seleniumServer = request.getSeleniumServer();
            user.setSeleniumServer(StringUtils.isBlank(seleniumServer) ? "" : seleniumServer.trim());
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

    public String verifyUserSeleniumServer() {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdEqualTo(SessionUtils.getUser().getId());
        List<User> users = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(users)) {
            User user = users.get(0);
            if (StringUtils.isBlank(user.getSeleniumServer())) {
                return "configErr";
            }
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType,
                    "{\"operationName\":\"\",\"variables\":{},\"query\":\"query Summary {\\n  grid {\\n    uri\\n    totalSlots\\n    nodeCount\\n    maxSession\\n    sessionCount\\n    sessionQueueSize\\n    version\\n    __typename\\n  }\\n}\"}");
            Request req = new Request.Builder()
                    .url(user.getSeleniumServer() + "/graphql")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = null;
            try {
                response = client.newCall(req).execute();
                if (!response.isSuccessful()) {
                    return "connectionErr";
                }
            } catch (Exception e) {
                return "connectionErr";
            } finally {
                try {
                    if (response != null) {
                        response.close();
                    }
                } catch (Exception e) {

                }
            }
        }
        return "ok";
    }

    public List<User> getProjectMemberOption(String projectId) {
        return extUserGroupMapper.getProjectMemberOption(projectId);
    }
}
