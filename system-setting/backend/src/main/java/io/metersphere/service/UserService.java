package io.metersphere.service;

import com.alibaba.excel.EasyExcelFactory;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseProjectMapper;
import io.metersphere.base.mapper.ext.BaseUserGroupMapper;
import io.metersphere.base.mapper.ext.BaseUserMapper;
import io.metersphere.base.mapper.ext.BaseWorkspaceMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.GroupResourceDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserGroupPermissionDTO;
import io.metersphere.dto.WorkspaceDTO;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.excel.domain.UserExcelData;
import io.metersphere.excel.domain.UserExcelDataFactory;
import io.metersphere.excel.listener.EasyExcelListener;
import io.metersphere.excel.listener.UserDataListener;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.request.UserRequest;
import io.metersphere.request.WorkspaceRequest;
import io.metersphere.request.member.AddMemberRequest;
import io.metersphere.request.member.EditPassWordRequest;
import io.metersphere.request.member.EditSeleniumServerRequest;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.request.resourcepool.UserBatchProcessRequest;
import io.metersphere.xpack.quota.service.QuotaService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.authc.DisabledAccountException;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private BaseUserMapper baseUserMapper;
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
    private BaseUserGroupMapper baseUserGroupMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    private BaseWorkspaceMapper baseWorkspaceMapper;

    public List<UserDetail> queryTypeByIds(List<String> userIds) {
        return baseUserMapper.queryTypeByIds(userIds);
    }

    public Map<String, User> queryNameByIds(List<String> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>(0);
        }
        return baseUserMapper.queryNameByIds(userIds);
    }

    public Map<String, User> queryName() {
        return baseUserMapper.queryName();
    }

    public UserDTO insert(io.metersphere.request.member.UserRequest userRequest) {
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
                QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
                checkQuota(quotaService, group.getType(), ids, Collections.singletonList(userId));
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

    private void checkQuota(QuotaService quotaService, String type, List<String> sourceIds, List<String> userIds) {
        if (quotaService != null) {
            Map<String, List<String>> addMemberMap = sourceIds.stream().collect(Collectors.toMap(id -> id, id -> userIds));
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

    public List<User> getUserListWithRequest(UserRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders(), "create_time"));
        return baseUserMapper.getUserList(request);
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

    public void updateUserRole(io.metersphere.request.member.UserRequest user) {
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

    public UserDTO getUserInfo(String userId) {
        return getUserDTO(userId);
    }


    public void addMember(AddMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {

            if (CollectionUtils.isNotEmpty(request.getUserIds())) {
                QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
                checkQuota(quotaService, "WORKSPACE", Collections.singletonList(request.getWorkspaceId()), request.getUserIds());
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
            user.setLastWorkspaceId(StringUtils.EMPTY);
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
            user.setLastWorkspaceId(StringUtils.EMPTY);
        }
        if (StringUtils.equals("workspace", sign) && StringUtils.equals(sourceId, user.getLastWorkspaceId())) {
            user.setLastWorkspaceId(StringUtils.EMPTY);
        }

        BeanUtils.copyProperties(user, newUser);

        SessionUtils.putUser(SessionUser.fromUser(user, SessionUtils.getSessionId()));
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
        return baseUserMapper.searchUser(condition);
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
        List<ExcelErrData<UserExcelData>> errList = null;
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
        return baseUserMapper.selectAllId();
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


        List<String> worksapceIds = request.getBatchProcessValue();
        if (CollectionUtils.isNotEmpty(userIds)) {
            QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
            checkQuota(quotaService, "WORKSPACE", worksapceIds, userIds);
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
                        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
                        checkQuota(quotaService, gp.getType(), list, Collections.singletonList(userId));
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


        List<String> projectIds = request.getBatchProcessValue();
        if (CollectionUtils.isNotEmpty(userIds)) {
            QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
            checkQuota(quotaService, "PROJECT", projectIds, userIds);
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
                userIdList = baseUserMapper.selectIdsByQuery(request.getCondition());
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

    public String getLogDetails(io.metersphere.request.member.UserRequest request) {
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
        return baseUserGroupMapper.getProjectMemberList(request);
    }

    public void addProjectMember(AddMemberRequest request) {
        this.addGroupMember("PROJECT", request.getProjectId(), request.getUserIds(), request.getGroupIds());

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


    public void updateImportUserGroup(io.metersphere.request.member.UserRequest user) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(user.getId());
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> list = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(list)) {
            if (list.contains(user.getLastWorkspaceId())) {
                user.setLastWorkspaceId(StringUtils.EMPTY);
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

    public void saveImportUser(io.metersphere.request.member.UserRequest userRequest) {
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
                    Group group = groupMapper.selectByPrimaryKey(groupId);
                    QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
                    checkQuota(quotaService, group.getType(), ids, Collections.singletonList(userId));
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
        SessionUtils.putUser(SessionUser.fromUser(userDTO, SessionUtils.getSessionId()));
        return SessionUtils.getUser();
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


    public List<User> getProjectMemberOption(String projectId) {
        return baseUserGroupMapper.getProjectMemberOption(projectId);
    }

    public void addWorkspaceMember(AddMemberRequest request) {
        this.addGroupMember("WORKSPACE", request.getWorkspaceId(), request.getUserIds(), request.getGroupIds());
    }

    /**
     * 添加项目｜工作空间成员
     *
     * @param type     项目｜工作空间
     * @param sourceId project_id | workspace_id
     * @param userIds  成员ID列表
     * @param groupIds 用户组ID列表
     */
    private void addGroupMember(String type, String sourceId, List<String> userIds, List<String> groupIds) {
        if (!StringUtils.equalsAny(type, "PROJECT", "WORKSPACE") || StringUtils.isBlank(sourceId)
                || CollectionUtils.isEmpty(userIds) || CollectionUtils.isEmpty(groupIds)) {
            LogUtil.info("add member warning, please check param!");
            return;
        }
        this.checkQuotaOfMemberSize(type, sourceId, userIds);
        List<String> dbOptionalGroupIds = this.getGroupIdsByType(type, sourceId);
        for (String userId : userIds) {
            User user = userMapper.selectByPrimaryKey(userId);
            if (user == null) {
                LogUtil.info("add member warning, invalid user id: " + userId);
                continue;
            }
            List<String> toAddGroupIds = new ArrayList<>(groupIds);
            List<String> existGroupIds = this.getUserExistSourceGroup(userId, sourceId);
            toAddGroupIds.removeAll(existGroupIds);
            toAddGroupIds.retainAll(dbOptionalGroupIds);
            for (String groupId : toAddGroupIds) {
                UserGroup userGroup = new UserGroup(UUID.randomUUID().toString(), userId, groupId,
                        sourceId, System.currentTimeMillis(), System.currentTimeMillis());
                userGroupMapper.insertSelective(userGroup);
            }
        }
    }

    /**
     * 向单个工作空间或单个项目中添加成员时检查成员数量配额
     *
     * @param type     PROJECT ｜ WORKSPACE
     * @param sourceId project_id | workspace_id
     * @param userIds  添加的用户id
     */
    private void checkQuotaOfMemberSize(String type, String sourceId, List<String> userIds) {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (CollectionUtils.isNotEmpty(userIds)) {
            checkQuota(quotaService, type, Collections.singletonList(sourceId), userIds);
        }
    }

    private List<String> getGroupIdsByType(String type, String sourceId) {
        // 某项目/工作空间下能查看到的用户组
        List<String> scopeList = Arrays.asList("global", sourceId);
        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andScopeIdIn(scopeList)
                .andTypeEqualTo(type);
        List<Group> groups = groupMapper.selectByExample(groupExample);
        return groups.stream().map(Group::getId).collect(Collectors.toList());
    }

    private List<String> getUserExistSourceGroup(String userId, String sourceId) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(sourceId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        return userGroups.stream().map(UserGroup::getGroupId).collect(Collectors.toList());
    }

}
