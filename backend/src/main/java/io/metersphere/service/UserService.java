package io.metersphere.service;

import com.alibaba.excel.EasyExcelFactory;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtOrganizationMapper;
import io.metersphere.base.mapper.ext.ExtUserMapper;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.controller.request.member.AddMemberRequest;
import io.metersphere.controller.request.member.EditPassWordRequest;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.controller.request.member.UserRequest;
import io.metersphere.controller.request.organization.AddOrgMemberRequest;
import io.metersphere.controller.request.organization.QueryOrgMemberRequest;
import io.metersphere.controller.request.resourcepool.UserBatchProcessRequest;
import io.metersphere.dto.OrganizationMemberDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserRoleDTO;
import io.metersphere.dto.WorkspaceDTO;
import io.metersphere.excel.domain.*;
import io.metersphere.excel.listener.EasyExcelListener;
import io.metersphere.excel.listener.TestCaseDataListener;
import io.metersphere.excel.listener.UserDataListener;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.security.MsUserToken;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import io.metersphere.xmind.XmindCaseParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.python.antlr.ast.Str;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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

    public List<UserDetail> queryTypeByIds(List<String> userIds) {
        return extUserMapper.queryTypeByIds(userIds);
    }

    public Map<String, User> queryNameByIds(List<String> userIds) {
        return extUserMapper.queryNameByIds(userIds);
    }

  /*  public List<String> queryEmailByIds(List<String> userIds) {
        return extUserMapper.queryTypeByIds(userIds);
    }*/

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
        List<Map<String, Object>> roles = user.getRoles();
        if (!roles.isEmpty()) {
            insertUserRole(roles, user.getId());
        }
        return getUserDTO(user.getId());
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
//                if (!map.keySet().contains("ids")) {
//                    MSException.throwException(role + " no source id");
//                }
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
        UserRoleDTO userRole = getUserRole(userId);
        userDTO.setUserRoles(Optional.ofNullable(userRole.getUserRoles()).orElse(new ArrayList<>()));
        userDTO.setRoles(Optional.ofNullable(userRole.getRoles()).orElse(new ArrayList<>()));
        return userDTO;
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

        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andUserIdEqualTo(userId);
        userRoleMapper.deleteByExample(example);

        userMapper.deleteByPrimaryKey(userId);
    }

    public void updateUserRole(UserRequest user) {
        String userId = user.getId();
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        List<String> list = userRoles.stream().map(UserRole::getSourceId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(list)) {
            if (list.contains(user.getLastWorkspaceId()) || list.contains(user.getLastOrganizationId())) {
                user.setLastOrganizationId("");
                user.setLastWorkspaceId("");
                userMapper.updateByPrimaryKeySelective(user);
            }
        }

        userRoleMapper.deleteByExample(userRoleExample);
        List<Map<String, Object>> roles = user.getRoles();
        if (!roles.isEmpty()) {
            insertUserRole(roles, user.getId());
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
            List<Workspace> workspaces = workspaceService.getWorkspaceListByOrgIdAndUserId(sourceId);
            if (workspaces.size() > 0) {
                user.setLastWorkspaceId(workspaces.get(0).getId());
            } else {
                user.setLastWorkspaceId("");
            }
        }
        if (StringUtils.equals("workspace", sign)) {
            Workspace workspace = workspaceMapper.selectByPrimaryKey(sourceId);
            user.setLastOrganizationId(workspace.getOrganizationId());
            user.setLastWorkspaceId(sourceId);
        }
        BeanUtils.copyProperties(user, newUser);
        // 切换工作空间或组织之后更新 session 里的 user
        SessionUtils.putUser(SessionUser.fromUser(user));
        userMapper.updateByPrimaryKeySelective(newUser);
    }

    public UserDTO getUserInfo(String userId) {
        return getUserDTO(userId);
    }

    public List<User> getMemberList(QueryMemberRequest request) {
        return extUserRoleMapper.getMemberList(request);
    }

    public void addMember(AddMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            for (String userId : request.getUserIds()) {
                UserRoleExample userRoleExample = new UserRoleExample();
                userRoleExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(request.getWorkspaceId());
                List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
                if (userRoles.size() > 0) {
                    MSException.throwException(Translator.get("user_already_exists"));
                } else {
                    for (String roleId : request.getRoleIds()) {
                        UserRole userRole = new UserRole();
                        userRole.setRoleId(roleId);
                        userRole.setSourceId(request.getWorkspaceId());
                        userRole.setUserId(userId);
                        userRole.setId(UUID.randomUUID().toString());
                        userRole.setUpdateTime(System.currentTimeMillis());
                        userRole.setCreateTime(System.currentTimeMillis());
                        userRoleMapper.insertSelective(userRole);
                    }
                }
            }
        }
    }

    public void deleteMember(String workspaceId, String userId) {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andRoleIdLike("%test%")
                .andUserIdEqualTo(userId).andSourceIdEqualTo(workspaceId);

        User user = userMapper.selectByPrimaryKey(userId);
        if (StringUtils.equals(workspaceId, user.getLastWorkspaceId())) {
            user.setLastWorkspaceId("");
            user.setLastOrganizationId("");
            userMapper.updateByPrimaryKeySelective(user);
        }

        userRoleMapper.deleteByExample(example);
    }

    public void addOrganizationMember(AddOrgMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            for (String userId : request.getUserIds()) {
                UserRoleExample userRoleExample = new UserRoleExample();
                userRoleExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(request.getOrganizationId());
                List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
                if (userRoles.size() > 0) {
                    MSException.throwException(Translator.get("user_already_exists") + ": " + userId);
                } else {
                    for (String roleId : request.getRoleIds()) {
                        UserRole userRole = new UserRole();
                        userRole.setId(UUID.randomUUID().toString());
                        userRole.setRoleId(roleId);
                        userRole.setSourceId(request.getOrganizationId());
                        userRole.setUserId(userId);
                        userRole.setUpdateTime(System.currentTimeMillis());
                        userRole.setCreateTime(System.currentTimeMillis());
                        userRoleMapper.insertSelective(userRole);
                    }
                }
            }
        }
    }

    public void delOrganizationMember(String organizationId, String userId) {

        List<String> resourceIds = workspaceService.getWorkspaceIdsOrgId(organizationId);
        resourceIds.add(organizationId);

        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId).andSourceIdIn(resourceIds);

        User user = userMapper.selectByPrimaryKey(userId);
        if (StringUtils.equals(organizationId, user.getLastOrganizationId())) {
            user.setLastWorkspaceId("");
            user.setLastOrganizationId("");
            userMapper.updateByPrimaryKeySelective(user);
        }

        userRoleMapper.deleteByExample(userRoleExample);
    }

    public List<User> getOrgMemberList(QueryOrgMemberRequest request) {
        return extUserRoleMapper.getOrgMemberList(request);
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
                // 自动选中组织，工作空间
                if (StringUtils.isEmpty(user.getLastOrganizationId())) {
                    List<UserRole> userRoles = user.getUserRoles();
                    List<UserRole> test = userRoles.stream().filter(ur -> ur.getRoleId().startsWith("test")).collect(Collectors.toList());
                    List<UserRole> org = userRoles.stream().filter(ur -> ur.getRoleId().startsWith("org")).collect(Collectors.toList());
                    if (test.size() > 0) {
                        String wsId = test.get(0).getSourceId();
                        switchUserRole("workspace", wsId);
                    } else if (org.size() > 0) {
                        String orgId = org.get(0).getSourceId();
                        switchUserRole("organization", orgId);
                    }
                }
                // 返回 userDTO
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
        List<String> types = TestCaseConstants.Type.getValues();
        List<String> methods = TestCaseConstants.Method.getValues();
        SessionUser user = SessionUtils.getUser();
        for (int i = 1; i <= 2; i++) {
            UserExcelData data = new UserExcelData();
            data.setId("user_id_" + i);
            data.setName(Translator.get("user") + i);
            String workspace = "";
            for (int workspaceIndex = 1; workspaceIndex <= i; workspaceIndex++) {
                if (workspaceIndex == 1) {
                    workspace = "workspace" + workspaceIndex;
                } else {
                    workspace = workspace + "\n" + "workspace" + workspaceIndex;
                }
            }
            data.setUserIsAdmin(Translator.get("options_no"));
            data.setUserIsTester(Translator.get("options_no"));
            data.setUserIsOrgMember(Translator.get("options_no"));
            data.setUserIsViewer(Translator.get("options_no"));
            data.setUserIsTestManager(Translator.get("options_no"));
            data.setUserIsOrgAdmin(Translator.get("options_yes"));
            data.setOrgAdminOrganization(workspace);
            list.add(data);
        }

        list.add(new UserExcelData());
        UserExcelData explain = new UserExcelData();
        explain.setName(Translator.get("do_not_modify_header_order"));
        explain.setOrgAdminOrganization("多个工作空间请换行展示");
        list.add(explain);
        return list;
    }

    public ExcelResponse userImport(MultipartFile multipartFile, String userId) {

        ExcelResponse excelResponse = new ExcelResponse();
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        List<ExcelErrData<TestCaseExcelData>> errList = null;
        if (multipartFile == null) {
            MSException.throwException(Translator.get("upload_fail"));
        }
        try {
            Class clazz = new UserExcelDataFactory().getExcelDataByLocal();

            Map<String, String> orgNameMap = new HashMap<>();
            Map<String, String> workspaceNameMap = new HashMap<>();

            List<OrganizationMemberDTO> organizationList = extOrganizationMapper.findIdAndNameByOrganizationId("All");
            for (OrganizationMemberDTO model : organizationList) {
                orgNameMap.put(model.getName(), model.getId());
            }
            List<WorkspaceDTO> workspaceList = workspaceService.findIdAndNameByOrganizationId("All");
            for (WorkspaceDTO model : workspaceList) {
                workspaceNameMap.put(model.getName(), model.getId());
            }
            EasyExcelListener easyExcelListener = new UserDataListener(clazz, workspaceNameMap, orgNameMap);
            EasyExcelFactory.read(multipartFile.getInputStream(), clazz, easyExcelListener).sheet().doRead();
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
        List<String> userIdList = this.selectIdByUserRequest(request);
        String batchType = request.getBatchType();
        for (String userID : userIdList) {
            Map<String, List<String>> roleResourceIdMap = new HashMap<>();
            if (StringUtils.equals(BatchProcessUserInfoType.ADD_WORKSPACE.name(), batchType)) {
                //添加工作空间时，默认赋予只读用户权限
                String userRole = RoleConstants.TEST_VIEWER;
                List<String> workspaceID = request.getBatchProcessValue();
                if (workspaceID != null && !workspaceID.isEmpty()) {
                    roleResourceIdMap.put(userRole, workspaceID);
                }
            } else if (StringUtils.equals(BatchProcessUserInfoType.ADD_USER_ROLE.name(), batchType)) {
                roleResourceIdMap = this.genRoleResourceMap(request.getBatchProcessValue());
            }
            if (!roleResourceIdMap.isEmpty()) {
                UserRoleExample userRoleExample = new UserRoleExample();
                userRoleExample.createCriteria().andUserIdEqualTo(userID);
                List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
                UserRequest user = this.convert2UserRequest(userID, roleResourceIdMap, userRoles);
                this.addUserWorkspaceAndRole(user, userRoles);
            }
        }
    }

    private List<String> selectIdByUserRequest(UserBatchProcessRequest request) {

        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            List<String> userIdList = new ArrayList<>();
            if(StringUtils.isEmpty(request.getOrganizationId())){
                userIdList = extUserMapper.selectIdsByQuery(request.getCondition());
            }else{
                //组织->成员 页面发起的请求
                userIdList = extUserRoleMapper.selectIdsByQuery(request.getOrganizationId(),request.getCondition());
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
}
