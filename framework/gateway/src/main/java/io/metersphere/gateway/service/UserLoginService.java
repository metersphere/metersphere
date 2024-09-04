package io.metersphere.gateway.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseProjectMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.GroupResourceDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserGroupPermissionDTO;
import io.metersphere.dto.dingtalk.DingTalkCreator;
import io.metersphere.dto.dingtalk.DingTalkInfoDTO;
import io.metersphere.dto.dingtalk.DingTalkTokenParamDTO;
import io.metersphere.dto.lark.LarkBaseParamDTO;
import io.metersphere.dto.lark.LarkCreator;
import io.metersphere.dto.lark.LarkInfoDTO;
import io.metersphere.dto.lark.LarkTokenParamDTO;
import io.metersphere.dto.wecom.WeComCreator;
import io.metersphere.dto.wecom.WeComInfoDTO;
import io.metersphere.gateway.client.QrCodeClient;
import io.metersphere.i18n.Translator;
import io.metersphere.log.service.OperatingLogService;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.request.LoginRequest;
import io.metersphere.request.member.UserRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;

import jakarta.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserLoginService {
    private static final String WE_COM = "WE_COM";

    private static final String DING = "DING_TALK";

    private static final String LARK = "LARK";

    private static final String LARK_SUITE = "LARK_SUITE";

    private static final String WE_COM_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";

    private static final String WE_COM_USERID_URL = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo?access_token=%s&code=%s";
    private static final String WE_COM_USERINFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=%s&userid=%s";
    private static final String DING_USER_INFO = "https://api.dingtalk.com/v1.0/contact/users/me";
    private static final String DING_USER_TOKEN_URL = "https://api.dingtalk.com/v1.0/oauth2/userAccessToken";

    private static final String LARK_USER_TOKEN_URL = "https://open.feishu.cn/open-apis/authen/v1/oidc/access_token";

    private static final String LARK_SUITE_USER_TOKEN_URL = "https://open.larksuite.com/open-apis/authen/v1/oidc/access_token";


    private static final String LARK_APP_TOKEN_URL = "https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal";

    private static final String LARK_SUITE_APP_TOKEN_URL = "https://open.larksuite.com/open-apis/auth/v3/app_access_token/internal";

    private static final String LARK_SUITE_USER_INFO_URL = "https://open.larksuite.com/open-apis/authen/v1/user_info";

    private static final String LARK_USER_INFO_URL = "https://open.feishu.cn/open-apis/authen/v1/user_info";


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
    @Resource
    private BaseProjectMapper baseProjectMapper;

    @Resource
    private PlatformSourceMapper platformSourceMapper;
    @Resource
    private QrCodeClient qrCodeClient;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private OperatingLogService operatingLogService;

    public Optional<SessionUser> login(LoginRequest request, WebSession session, Locale locale) {
        UserDTO userDTO;
        if (locale != null) {
            LocaleContextHolder.setLocale(locale, true);
        }
        switch (request.getAuthenticate()) {
            case "OIDC":
            case "CAS":
            case "OAuth2":
                userDTO = loginSsoMode(request.getUsername(), request.getAuthenticate());
                break;
            case "LDAP":
                userDTO = loginLdapMode(request.getUsername());
                break;
            default:
                userDTO = loginLocalMode(request.getUsername(), request.getPassword());
                break;
        }
        autoSwitch(session, userDTO);
        SessionUser sessionUser = SessionUser.fromUser(userDTO, session.getId());
        session.getAttributes().put(SessionConstants.ATTR_USER, sessionUser);
        return Optional.of(sessionUser);
    }

    private UserDTO loginLdapMode(String userId) {
        // LDAP验证通过之后，如果用户存在且用户类型是LDAP或LOCAL，返回用户
        UserDTO loginUser = getLoginUser(userId, Arrays.asList(UserSource.LDAP.name(), UserSource.LOCAL.name()));
        if (loginUser == null) {
            MSException.throwException(Translator.get("user_not_found_or_not_unique"));
        }
        return loginUser;
    }

    private UserDTO loginSsoMode(String userId, String authType) {
        return getLoginUser(userId, Collections.singletonList(authType));
    }

    public UserDTO loginLocalMode(String userId, String password) {
        UserDTO user = getLoginUser(userId, List.of(UserSource.LOCAL.name(), UserSource.QR_CODE.name()));
        if (user == null) {
            user = getUserDTOByEmail(userId, UserSource.LOCAL.name(), UserSource.QR_CODE.name());
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

    public void autoSwitch(WebSession session, UserDTO user) {
        // 用户有 last_project_id 权限
        if (hasLastProjectPermission(user)) {
            return;
        }
        // 用户有 last_workspace_id 权限
        if (hasLastWorkspacePermission(user)) {
            return;
        }
        // 判断其他权限
        checkNewWorkspaceAndProject(session, user);
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

    private void checkNewWorkspaceAndProject(WebSession session, UserDTO user) {
        List<UserGroup> userGroups = user.getUserGroups();
        List<Group> groups = user.getGroups();

        List<String> projectGroupIds = groups
                .stream()
                .filter(ug -> StringUtils.equals(ug.getType(), UserGroupType.PROJECT))
                .map(Group::getId)
                .collect(Collectors.toList());

        List<UserGroup> projects = userGroups
                .stream()
                .filter(ug -> projectGroupIds.contains(ug.getGroupId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(projects)) {
            List<String> workspaceIds = groups
                    .stream()
                    .filter(ug -> StringUtils.equals(ug.getType(), UserGroupType.WORKSPACE))
                    .map(Group::getId)
                    .collect(Collectors.toList());

            List<UserGroup> workspaces = userGroups
                    .stream()
                    .filter(ug -> workspaceIds.contains(ug.getGroupId()))
                    .collect(Collectors.toList());

            if (workspaces.size() > 0) {
                String wsId = workspaces.get(0).getSourceId();
                switchUserResource(session, "workspace", wsId, user);
            } else {
                List<String> superGroupIds = groups
                        .stream()
                        .map(Group::getId)
                        .filter(id -> StringUtils.equals(id, UserGroupConstants.SUPER_GROUP))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(superGroupIds)) {
                    Project p = baseProjectMapper.selectOne();
                    if (p != null) {
                        switchSuperUserResource(session, p.getId(), p.getWorkspaceId(), user);
                    }
                } else {
                    // 用户登录之后没有项目和工作空间的权限就把值清空
                    user.setLastWorkspaceId(StringUtils.EMPTY);
                    user.setLastProjectId(StringUtils.EMPTY);
                    updateUser(user);
                }
            }
        } else {
            UserGroup userGroup = projects.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getSourceId()))
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

    public void switchUserResource(WebSession session, String sign, String sourceId, UserDTO sessionUser) {
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
        session.getAttributes().put(SessionConstants.ATTR_USER, SessionUser.fromUser(user, session.getId()));
        session.getAttributes().put(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, sessionUser.getId());
        userMapper.updateByPrimaryKeySelective(newUser);
    }

    private void switchSuperUserResource(WebSession session, String projectId, String workspaceId, UserDTO sessionUser) {
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();
        user.setLastWorkspaceId(workspaceId);
        sessionUser.setLastWorkspaceId(workspaceId);
        user.setLastProjectId(projectId);
        BeanUtils.copyProperties(user, newUser);
        // 切换工作空间或组织之后更新 session 里的 user
        session.getAttributes().put(SessionConstants.ATTR_USER, SessionUser.fromUser(user, session.getId()));
        session.getAttributes().put(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, sessionUser.getId());
        userMapper.updateByPrimaryKeySelective(newUser);
    }

    public UserDTO getLoginUser(String userId, List<String> list) {
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andSourceIn(list);
        long count = userMapper.countByExample(example);
        if (count == 0) {
            LogUtil.error("get login user error, userid is {}, sources is {}", userId, list);
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
        if (userId.length() > 64) {
            MSException.throwException(Translator.get("user_id_length_too_long"));
        }
        if (password.length() > 50) {
            MSException.throwException(Translator.get("password_length_too_long"));
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

    public User addLdapUser(User user) {
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setStatus(UserStatus.NORMAL);
        checkEmailIsExist(user.getEmail());
        userMapper.insertSelective(user);
        return user;
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


    public String validateCsrfToken(String sessionId, String csrfToken) {
        if (StringUtils.isBlank(csrfToken)) {
            throw new RuntimeException("csrf token is empty");
        }
        csrfToken = CodingUtil.aesDecrypt(csrfToken, SessionUser.secret, SessionUser.iv);

        String[] signatureArray = StringUtils.split(StringUtils.trimToNull(csrfToken), "|");
        if (signatureArray.length != 4) {
            throw new RuntimeException("invalid token");
        }
        if (!StringUtils.equals(sessionId, signatureArray[2])) {
            throw new RuntimeException("Please check csrf token.");
        }
        return signatureArray[0];
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

    public Optional<SessionUser> exchangeWeComToken(String code, WebSession session, Locale locale) {
        WeComInfoDTO vo = getWeComInfo(WE_COM);
        String accessToken = generateWeComToken(vo);
        String url = String.format(WE_COM_USERID_URL, accessToken, code);
        String body = qrCodeClient.get(url);
        Map bodyMap = JSON.parseMap(body);
        if (ObjectUtils.isNotEmpty(bodyMap.get("errcode")) && Integer.parseInt(bodyMap.get("errcode").toString()) != 0) {
            MSException.throwException("获取USERID失败:" + bodyMap.get("errmsg"));
        }
        if (!bodyMap.containsKey("userid") && bodyMap.containsKey("openid")) {
            MSException.throwException("当前用户非企业成员，禁止登录操作");
        }
        String userId = bodyMap.get("userid").toString();
        String userInfoUrl = String.format(WE_COM_USERINFO_URL, accessToken, userId);
        String userJson = qrCodeClient.get(userInfoUrl);
        Map userMap = JSON.parseMap(userJson);
        if (ObjectUtils.isNotEmpty(userMap.get("errcode")) && Integer.parseInt(userMap.get("errcode").toString()) != 0) {
            MSException.throwException("获取用户详情失败:" + userMap.get("errmsg"));
        }
        String mobile = ObjectUtils.isNotEmpty(userMap.get("mobile")) ? userMap.get("mobile").toString() : "";
        String name = userMap.get("name").toString();
        String email = ObjectUtils.isNotEmpty(userMap.get("email")) ? userMap.get("email").toString() : ObjectUtils.isNotEmpty(userMap.get("biz_mail")) ? userMap.get("biz_mail").toString() : "";
        UserRequest userCreateInfo = getUserRequest(userId, email, name, mobile);
        return login(userCreateInfo, UserSource.QR_CODE.name(), session, locale);
    }


    public Optional<SessionUser> login(UserRequest userCreateInfo, String source, WebSession session, Locale locale) {
        String userId = userCreateInfo.getId();
        String email = userCreateInfo.getEmail();
        String name = userCreateInfo.getName();
        String phone = userCreateInfo.getPhone();
        //区分有无email
        UserDTO userDTOByEmail;
        boolean changeMail = false;
        try {
            UserDTO userDTO = getUserDTO(userId);
            if (StringUtils.isNotBlank(email)) {
                userDTOByEmail = getUserDTOByEmail(email);
                if (userDTO != null && userDTOByEmail == null) {
                    userDTOByEmail = new UserDTO();
                    BeanUtils.copyProperties(userDTO, userDTOByEmail);
                    userDTOByEmail.setEmail(email);
                    changeMail = true;
                }
            } else {
                email = userId + "@metersphere.io";
                userDTOByEmail = getUserDTOByEmail(email);
                if (userDTOByEmail == null) {
                    userDTOByEmail = getUserDTO(userId);
                }
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
        if (userDTOByEmail == null) {
            LogUtil.info("creatUser");
            LogUtil.info(email);
            LogUtil.info(name);
            LogUtil.info(userId);
            userCreateInfo.setEmail(email);
            creatUser(userCreateInfo, source);
        } else {
            userId = userDTOByEmail.getId();
            if (StringUtils.equals(userDTOByEmail.getStatus(), UserStatus.DISABLED)) {
                MSException.throwException("user is disabled!");
            }
            LogUtil.info(userId);
            LogUtil.info(userDTOByEmail.getStatus());
            if (changeMail) {
                updateUser(email, userId);
            }
        }
        LogUtil.info("login user");
        LogUtil.info(email);
        LogUtil.info(name);
        LogUtil.info(userId);
        LoginRequest request = new LoginRequest();
        try {
            request.setAuthenticate(source);
            request.setUsername(userId);
            request.setPassword(email);
        } catch (Exception e) {
            LogUtil.error("login error: ", e);
            MSException.throwException("login error: " + e.getMessage());
        }

        return login(request, session, locale);
    }

    private void updateUser(String email, String userId) {
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setEmail(email);
        updateUser.setUpdateTime(System.currentTimeMillis());
        updateUser.setCreateUser(null);
        updateUser.setUpdateTime(null);
        userMapper.updateByPrimaryKeySelective(updateUser);
    }



    public WeComInfoDTO getWeComInfo(String key) {
        PlatformSource platformSource = platformSourceMapper.selectByPrimaryKey(key);
        WeComInfoDTO weComInfoDTO = new WeComInfoDTO();
        WeComCreator weComCreator = JSON.parseObject(platformSource.getConfig(), WeComCreator.class);
        BeanUtils.copyProperties(weComCreator, weComInfoDTO);
        weComInfoDTO.setEnable(platformSource.getEnable());
        weComInfoDTO.setValid(platformSource.getValid());
        return weComInfoDTO;
    }


    public DingTalkInfoDTO getDingInfo(String key) {
        PlatformSource platformSource = platformSourceMapper.selectByPrimaryKey(key);
        DingTalkInfoDTO dingTalkInfoDTO = new DingTalkInfoDTO();
        DingTalkCreator dingTalkCreator = JSON.parseObject(platformSource.getConfig(), DingTalkCreator.class);
        BeanUtils.copyProperties(dingTalkCreator, dingTalkInfoDTO);
        dingTalkInfoDTO.setEnable(platformSource.getEnable());
        dingTalkInfoDTO.setValid(platformSource.getValid());
        return dingTalkInfoDTO;
    }

    private String generateWeComToken(WeComInfoDTO vo) {
        if (ObjectUtils.isEmpty(vo)) {
            vo = getWeComInfo(WE_COM);
        }
        String corpid = vo.getCorpId();
        String appSecret = vo.getAppSecret();
        String url = String.format(WE_COM_TOKEN_URL, corpid, appSecret);
        String body = qrCodeClient.get(url);
        Map bodyMap = JSON.parseMap(body);
        if (ObjectUtils.isNotEmpty(bodyMap.get("errcode")) && Integer.parseInt(bodyMap.get("errcode").toString()) != 0) {
            throw new RuntimeException("获取accessToken失败:" + bodyMap.get("errmsg"));
        }
        return bodyMap.get("access_token").toString();
    }

    private void creatUser(UserRequest userCreateInfo, String source) {
        User user = new User();
        BeanUtils.copyProperties(userCreateInfo, user);
        user.setCreateTime(System.currentTimeMillis());
        user.setCreateUser("admin");
        user.setUpdateTime(System.currentTimeMillis());
        // 默认1:启用状态
        user.setStatus(UserStatus.NORMAL);
        user.setSource(source);
        // 密码使用 MD5
        user.setEmail(user.getEmail());
        user.setPassword(CodingUtil.md5(user.getEmail()));
        userMapper.insertSelective(user);
        //获取默认空间
        Workspace workspace = getWorkspace();
        //获取默认项目
        Project project = getProject(workspace);
        //添加用户组
        addRole(workspace, project, user);
        //添加日志
        addLog(user, project);

    }

    public static void main(String[] args) {
        String s = CodingUtil.md5("MiPiSU5wRvpn9JcmUsYJubXAiEiE@metersphere.io");
        System.out.println(s);
    }

    private void addLog(User user, Project project) {
        OperatingLogWithBLOBs log = new OperatingLogWithBLOBs();
        log.setOperTitle(user.getName());
        log.setOperContent(user.getName());
        log.setProjectId(project.getId());
        log.setOperPath("/sso/callback/we_com");
        log.setId(UUID.randomUUID().toString());
        log.setOperType(OperLogConstants.CREATE.name());
        log.setOperModule(OperLogModule.SYSTEM_PARAMETER_SETTING);
        List<DetailColumn> columns = new LinkedList<>();
        OperatingLogDetails details = new OperatingLogDetails(user.getId(), project.getId(), user.getName(),
                user.getCreateUser(), columns);
        log.setOperContent(JSON.toJSONString(details));
        log.setOperTime(System.currentTimeMillis());
        log.setCreateUser(user.getCreateUser());
        log.setOperUser(user.getCreateUser());
        log.setSourceId(user.getId());
        operatingLogService.create(log, log.getSourceId());
    }

    private void addRole(Workspace workspace, Project project, User user) {
        Map<String, String> userRoleMap = new HashMap<>();
        userRoleMap.put(UserGroupConstants.WS_MEMBER, workspace.getId());
        userRoleMap.put(UserGroupConstants.PROJECT_MEMBER, project.getId());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserGroupMapper batchSaveMapper = sqlSession.getMapper(UserGroupMapper.class);
        userRoleMap.forEach((k, v) -> {
            UserGroup userGroup = new UserGroup();
            userGroup.setId(UUID.randomUUID().toString());
            userGroup.setUserId(user.getId());
            userGroup.setGroupId(k);
            userGroup.setSourceId(v);
            userGroup.setCreateTime(System.currentTimeMillis());
            userGroup.setUpdateTime(System.currentTimeMillis());
            batchSaveMapper.insertSelective(userGroup);
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    private Project getProject(Workspace Workspace) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.setOrderByClause("create_time ASC");
        projectExample.createCriteria().andWorkspaceIdEqualTo(Workspace.getId()).andVersionEnableEqualTo(true);
        List<Project> projects = projectMapper.selectByExample(projectExample);
        return projects.get(0);
    }

    private Workspace getWorkspace() {
        WorkspaceExample WorkspaceExample = new WorkspaceExample();
        WorkspaceExample.setOrderByClause("create_time ASC");
        List<Workspace> Workspaces = workspaceMapper.selectByExample(WorkspaceExample);
        return Workspaces.get(0);
    }


    public Optional<SessionUser> exchangeDingTalkToken(String authCode, WebSession session, Locale locale) {
        DingTalkInfoDTO vo = getDingInfo(DING);
        String dingToken = generateDingUserToken(vo, authCode);
        String body = qrCodeClient.exchange(DING_USER_INFO, dingToken, "x-acs-dingtalk-access-token", MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        Map bodyMap = JSON.parseMap(body);
        String userId = bodyMap.get("unionId").toString();
        String name = bodyMap.get("nick").toString();
        String email = ObjectUtils.isNotEmpty(bodyMap.get("email")) ? bodyMap.get("email").toString() : "";
        String mobile = ObjectUtils.isNotEmpty(bodyMap.get("mobile")) ? bodyMap.get("mobile").toString() : "";
        UserRequest userCreateInfo = getUserRequest(userId, email, name, mobile);
        return login(userCreateInfo, UserSource.QR_CODE.name(), session, locale);
    }


    public String generateDingUserToken(DingTalkInfoDTO vo, String code) {
        if (ObjectUtils.isEmpty(vo)) {
            vo = getDingInfo(DING);
        }
        String appKey = vo.getAppKey();
        String appSecret = vo.getAppSecret();
        DingTalkTokenParamDTO dingTalkTokenParamDTO = new DingTalkTokenParamDTO();
        dingTalkTokenParamDTO.setClientId(appKey);
        dingTalkTokenParamDTO.setClientSecret(appSecret);
        dingTalkTokenParamDTO.setCode(code);
        dingTalkTokenParamDTO.setGrantType("authorization_code");
        String body = qrCodeClient.postExchange(DING_USER_TOKEN_URL, null, null, dingTalkTokenParamDTO, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        Map bodyMap = JSON.parseMap(body);
        if (ObjectUtils.isNotEmpty(bodyMap.get("errcode")) && Integer.parseInt(bodyMap.get("errcode").toString()) != 0) {
            throw new RuntimeException("获取accessToken失败:" + bodyMap.get("errmsg"));
        }
        return bodyMap.get("accessToken").toString();
    }

    public LarkInfoDTO getLarkInfo(String key) {
        PlatformSource platformSource = platformSourceMapper.selectByPrimaryKey(key);
        if (platformSource == null) {
            return new LarkInfoDTO();
        }
        LarkInfoDTO LarkInfoDTO = new LarkInfoDTO();
        LarkCreator larkCreator = JSON.parseObject(platformSource.getConfig(), LarkCreator.class);
        BeanUtils.copyProperties(larkCreator, LarkInfoDTO);
        LarkInfoDTO.setEnable(platformSource.getEnable());
        LarkInfoDTO.setValid(platformSource.getValid());
        return LarkInfoDTO;
    }

    public String generateLarkAppToken(LarkInfoDTO vo, String url, String key) {
        if (ObjectUtils.isEmpty(vo)) {
            vo = getLarkInfo(key);
        }
        String agentId = vo.getAgentId();
        String appSecret = vo.getAppSecret();
        LarkBaseParamDTO larkBaseParamDTO = new LarkBaseParamDTO();
        larkBaseParamDTO.setApp_id(agentId);
        larkBaseParamDTO.setApp_secret(appSecret);
        String body = qrCodeClient.postExchange(url, null, null, larkBaseParamDTO, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        Map bodyMap = JSON.parseMap(body);
        if (ObjectUtils.isNotEmpty(bodyMap.get("code")) && Integer.parseInt(bodyMap.get("code").toString()) > 0) {
            throw new RuntimeException("获取appAccessToken失败:" + bodyMap.get("msg"));
        }
        return bodyMap.get("app_access_token").toString();
    }

    public String generateLarkUserToken(String authCode, String url, String larkAppToken) {
        LarkTokenParamDTO larkTokenParamDTO = new LarkTokenParamDTO();
        larkTokenParamDTO.setCode(authCode);
        larkTokenParamDTO.setGrant_type("authorization_code");
        String body = qrCodeClient.postExchange(url, "Bearer " + larkAppToken, HttpHeaders.AUTHORIZATION, larkTokenParamDTO, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        Map bodyMap = JSON.parseMap(body);
        if (ObjectUtils.isNotEmpty(bodyMap.get("code")) && Integer.parseInt(bodyMap.get("code").toString()) > 0) {
            throw new RuntimeException("获取user_access_token失败:" + bodyMap.get("msg"));
        }
        Object o = bodyMap.get("data");
        return ((Map) o).get("access_token").toString();
    }


    public Optional<SessionUser> exchangeLarkToken(String authCode, WebSession session, Locale locale) {
        LarkInfoDTO vo = getLarkInfo(LARK);
        String larkAppToken = generateLarkAppToken(vo, LARK_APP_TOKEN_URL, LARK);
        String larkUserToken = generateLarkUserToken(authCode, LARK_USER_TOKEN_URL, larkAppToken);
        String body = qrCodeClient.exchange(LARK_USER_INFO_URL, "Bearer " + larkUserToken, HttpHeaders.AUTHORIZATION, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        Map bodyMap = JSON.parseMap(body);
        Object uMapobj = bodyMap.get("data");
        Map userMap = (Map) uMapobj;
        String userId = userMap.get("union_id").toString();
        String name = userMap.get("name").toString();
        String email = ObjectUtils.isNotEmpty(userMap.get("email")) ? userMap.get("email").toString() : ObjectUtils.isNotEmpty(userMap.get("enterprise_email")) ? userMap.get("enterprise_email").toString() : "";
        String mobile = ObjectUtils.isNotEmpty(userMap.get("mobile")) ? userMap.get("mobile").toString() : "";
        UserRequest userCreateInfo = getUserRequest(userId, email, name, mobile);
        return login(userCreateInfo, UserSource.QR_CODE.name(), session, locale);
    }

    public Optional<SessionUser> exchangeLarkSuiteToken(String authCode, WebSession session, Locale locale) {
        LarkInfoDTO vo = getLarkInfo(LARK_SUITE);
        String larkAppToken = generateLarkAppToken(vo, LARK_SUITE_APP_TOKEN_URL, LARK);
        String larkUserToken = generateLarkUserToken(authCode, LARK_SUITE_USER_TOKEN_URL, larkAppToken);
        String body = qrCodeClient.exchange(LARK_SUITE_USER_INFO_URL, "Bearer " + larkUserToken, HttpHeaders.AUTHORIZATION, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        Map bodyMap = JSON.parseMap(body);
        Object uMapobj = bodyMap.get("data");
        Map userMap = (Map) uMapobj;
        String userId = userMap.get("union_id").toString();
        String name = userMap.get("name").toString();
        String email = ObjectUtils.isNotEmpty(userMap.get("email")) ? userMap.get("email").toString() : ObjectUtils.isNotEmpty(userMap.get("enterprise_email")) ? userMap.get("enterprise_email").toString() : "";
        String mobile = ObjectUtils.isNotEmpty(userMap.get("mobile")) ? userMap.get("mobile").toString() : "";
        UserRequest userCreateInfo = getUserRequest(userId, email, name, mobile);
        return login(userCreateInfo, UserSource.QR_CODE.name(), session, locale);
    }

    @NotNull
    private static UserRequest getUserRequest(String userId, String email, String name, String mobile) {
        UserRequest userCreateInfo = new UserRequest();
        userCreateInfo.setId(userId);
        userCreateInfo.setEmail(email);
        userCreateInfo.setName(name);
        userCreateInfo.setPhone(mobile);
        return userCreateInfo;
    }
}
