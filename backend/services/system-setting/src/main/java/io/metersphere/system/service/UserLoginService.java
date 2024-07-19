package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.constants.UserSource;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CodingUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.sdk.LoginRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.UserExcludeOptionDTO;
import io.metersphere.system.dto.user.UserRolePermissionDTO;
import io.metersphere.system.dto.user.UserRoleResourceDTO;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.*;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional(rollbackFor = Exception.class)
public class UserLoginService {
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRolePermissionMapper userRolePermissionMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private OperationLogService operationLogService;

    public UserDTO getUserDTO(String userId) {
        UserDTO userDTO = baseUserMapper.selectById(userId);
        if (userDTO == null) {
            return null;
        }
        if (BooleanUtils.isFalse(userDTO.getEnable())) {
            throw new DisabledAccountException();
        }
        UserRolePermissionDTO dto = getUserRolePermission(userId);
        userDTO.setUserRoleRelations(dto.getUserRoleRelations());
        userDTO.setUserRoles(dto.getUserRoles());
        userDTO.setUserRolePermissions(dto.getList());
        return userDTO;
    }

    public ResultHolder login(LoginRequest request) {
        String login = (String) SecurityUtils.getSubject().getSession().getAttribute("authenticate");
        String username = StringUtils.trim(request.getUsername());
        String password = StringUtils.EMPTY;
        if (!StringUtils.equals(login, UserSource.LDAP.name())) {
            password = StringUtils.trim(request.getPassword());
        }
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            saveLog(SessionUtils.getUserId(), HttpMethodConstants.POST.name(), "/login", "登录成功", OperationLogType.LOGIN.name());
            if (subject.isAuthenticated()) {
                SessionUser sessionUser = SessionUtils.getUser();
                autoSwitch(sessionUser);
                // 放入session中
                SessionUtils.putUser(sessionUser);
                return ResultHolder.success(sessionUser);
            } else {
                throw new MSException(Translator.get("login_fail"));
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

    //保存日志
    public void saveLog(String userId, String method, String path, String content, String type) {
        User user = userMapper.selectByPrimaryKey(userId);
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                userId,
                type,
                OperationLogConstants.SYSTEM,
                StringUtils.join(user.getName(), StringUtils.EMPTY, content));
        dto.setMethod(method);
        dto.setPath(path);
        operationLogService.add(dto);
    }

    public void autoSwitch(UserDTO user) {
        // 判断是否是系统管理员
        if (isSystemAdmin(user)) {
            return;
        }
        // 用户有 last_project_id 权限
        if (hasLastProjectPermission(user)) {
            return;
        }
        // 用户有 last_organization_id 权限
        if (hasLastOrganizationPermission(user)) {
            return;
        }
        // 判断其他权限
        checkNewOrganizationAndProject(user);
    }

    private void checkNewOrganizationAndProject(UserDTO user) {
        List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations();
        List<String> projectRoleIds = user.getUserRoles()
                .stream().filter(ug -> StringUtils.equals(ug.getType(), UserRoleType.PROJECT.name()))
                .map(UserRole::getId)
                .toList();
        List<UserRoleRelation> project = userRoleRelations.stream().filter(ug -> projectRoleIds.contains(ug.getRoleId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(project)) {
            List<String> organizationIds = user.getUserRoles()
                    .stream()
                    .filter(ug -> StringUtils.equals(ug.getType(), UserRoleType.ORGANIZATION.name()))
                    .map(UserRole::getId)
                    .toList();
            List<UserRoleRelation> organizations = userRoleRelations.stream().filter(ug -> organizationIds.contains(ug.getRoleId()))
                    .toList();
            if (CollectionUtils.isNotEmpty(organizations)) {
                //获取所有的组织
                List<String> orgIds = organizations.stream().map(UserRoleRelation::getSourceId).collect(Collectors.toList());
                OrganizationExample organizationExample = new OrganizationExample();
                organizationExample.createCriteria().andIdIn(orgIds).andEnableEqualTo(true);
                List<Organization> organizationsList = organizationMapper.selectByExample(organizationExample);
                if (CollectionUtils.isNotEmpty(organizationsList)) {
                    String wsId = organizationsList.getFirst().getId();
                    switchUserResource(wsId, user);
                }
            } else {
                // 用户登录之后没有项目和组织的权限就把值清空
                user.setLastOrganizationId(StringUtils.EMPTY);
                user.setLastProjectId(StringUtils.EMPTY);
                updateUser(user);
            }
        } else {
            UserRoleRelation userRoleRelation = project.stream().filter(p -> StringUtils.isNotBlank(p.getSourceId()))
                    .toList().getFirst();
            String projectId = userRoleRelation.getSourceId();
            Project p = projectMapper.selectByPrimaryKey(projectId);
            String wsId = p.getOrganizationId();
            user.setId(user.getId());
            user.setLastProjectId(projectId);
            user.setLastOrganizationId(wsId);
            updateUser(user);
        }
    }

    private boolean hasLastProjectPermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastProjectId())) {
            List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations().stream()
                    .filter(ug -> StringUtils.equals(user.getLastProjectId(), ug.getSourceId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                ProjectExample example = new ProjectExample();
                example.createCriteria().andIdEqualTo(user.getLastProjectId()).andEnableEqualTo(true);
                List<Project> projects = projectMapper.selectByExample(example);
                if (CollectionUtils.isNotEmpty(projects)) {
                    Project project = projects.getFirst();
                    if (StringUtils.equals(project.getOrganizationId(), user.getLastOrganizationId())) {
                        return true;
                    }
                    // last_project_id 和 last_organization_id 对应不上了
                    user.setLastOrganizationId(project.getOrganizationId());
                    updateUser(user);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSystemAdmin(UserDTO user) {
        if (baseUserMapper.isSuperUser(user.getId())) {
            // 如果是系统管理员，判断是否有项目权限
            if (StringUtils.isNotBlank(user.getLastProjectId())) {
                ProjectExample example = new ProjectExample();
                example.createCriteria().andIdEqualTo(user.getLastProjectId()).andEnableEqualTo(true);
                List<Project> projects = projectMapper.selectByExample(example);
                if (CollectionUtils.isNotEmpty(projects)) {
                    Project project = projects.getFirst();
                    if (StringUtils.equals(project.getOrganizationId(), user.getLastOrganizationId())) {
                        return true;
                    }
                    // last_project_id 和 last_organization_id 对应不上了
                    user.setLastOrganizationId(project.getOrganizationId());
                    updateUser(user);
                    return true;
                }
            }
            // 项目没有权限  则取当前组织下的第一个项目
            if (StringUtils.isNotBlank(user.getLastOrganizationId())) {
                OrganizationExample organizationExample = new OrganizationExample();
                organizationExample.createCriteria().andIdEqualTo(user.getLastOrganizationId()).andEnableEqualTo(true);
                List<Organization> organizations = organizationMapper.selectByExample(organizationExample);
                if (CollectionUtils.isNotEmpty(organizations)) {
                    Organization organization = organizations.getFirst();
                    ProjectExample projectExample = new ProjectExample();
                    projectExample.createCriteria().andOrganizationIdEqualTo(organization.getId()).andEnableEqualTo(true);
                    List<Project> projectList = projectMapper.selectByExample(projectExample);
                    if (CollectionUtils.isNotEmpty(projectList)) {
                        Project project = projectList.getFirst();
                        user.setLastProjectId(project.getId());
                        updateUser(user);
                        return true;
                    } else {
                        // 组织下无项目, 走前端逻辑, 跳转到无项目的路由
                        updateUser(user);
                        return true;
                    }
                }
            }
            //项目和组织都没有权限
            Project project = baseUserMapper.getEnableProjectAndOrganization();
            if (project != null) {
                user.setLastProjectId(project.getId());
                user.setLastOrganizationId(project.getOrganizationId());
                updateUser(user);
                return true;
            }
            return true;

        }
        return false;
    }

    private boolean hasLastOrganizationPermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastOrganizationId())) {
            OrganizationExample organizationExample = new OrganizationExample();
            organizationExample.createCriteria().andIdEqualTo(user.getLastOrganizationId()).andEnableEqualTo(true);
            List<Organization> organizations = organizationMapper.selectByExample(organizationExample);
            if (CollectionUtils.isEmpty(organizations)) {
                return false;
            }
            List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations().stream()
                    .filter(ug -> StringUtils.equals(user.getLastOrganizationId(), ug.getSourceId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                ProjectExample example = new ProjectExample();
                example.createCriteria().andOrganizationIdEqualTo(user.getLastOrganizationId()).andEnableEqualTo(true);
                List<Project> projects = projectMapper.selectByExample(example);
                // 组织下没有项目
                if (CollectionUtils.isEmpty(projects)) {
                    user.setLastProjectId(StringUtils.EMPTY);
                    updateUser(user);
                    return true;
                }
                // 组织下有项目，选中有权限的项目
                List<String> projectIds = projects.stream()
                        .map(Project::getId)
                        .toList();

                List<UserRoleRelation> roleRelations = user.getUserRoleRelations();
                List<String> projectRoleIds = user.getUserRoles()
                        .stream().filter(ug -> StringUtils.equals(ug.getType(), UserRoleType.PROJECT.name()))
                        .map(UserRole::getId)
                        .toList();
                List<String> projectIdsWithPermission = roleRelations.stream().filter(ug -> projectRoleIds.contains(ug.getRoleId()))
                        .map(UserRoleRelation::getSourceId)
                        .filter(StringUtils::isNotBlank)
                        .filter(projectIds::contains)
                        .toList();

                List<String> intersection = projectIds.stream().filter(projectIdsWithPermission::contains).collect(Collectors.toList());
                // 当前组织下的所有项目都没有权限
                if (CollectionUtils.isEmpty(intersection)) {
                    user.setLastProjectId(StringUtils.EMPTY);
                    updateUser(user);
                    return true;
                }
                Optional<Project> first = projects.stream().filter(p -> StringUtils.equals(intersection.getFirst(), p.getId())).findFirst();
                if (first.isPresent()) {
                    Project project = first.get();
                    String wsId = project.getOrganizationId();
                    user.setId(user.getId());
                    user.setLastProjectId(project.getId());
                    user.setLastOrganizationId(wsId);
                    updateUser(user);
                    return true;
                }
            }
        }
        return false;
    }


    public void switchUserResource(String sourceId, UserDTO sessionUser) {
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();
        user.setLastOrganizationId(sourceId);
        sessionUser.setLastOrganizationId(sourceId);
        user.setLastProjectId(StringUtils.EMPTY);
        List<Project> projects = getProjectListByWsAndUserId(sessionUser.getId(), sourceId);
        if (CollectionUtils.isNotEmpty(projects)) {
            user.setLastProjectId(projects.getFirst().getId());
        }
        BeanUtils.copyProperties(user, newUser);
        // 切换组织或组织之后更新 session 里的 user
        SessionUtils.putUser(SessionUser.fromUser(user, SessionUtils.getSessionId()));
        userMapper.updateByPrimaryKeySelective(newUser);
    }

    public void updateUser(User user) {
        // todo 提取重复代码
        if (StringUtils.isNotBlank(user.getEmail())) {
            UserExample example = new UserExample();
            UserExample.Criteria criteria = example.createCriteria();
            criteria.andEmailEqualTo(user.getEmail());
            criteria.andIdNotEqualTo(user.getId());
            if (userMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("user_email_already_exists"));
            }
        }
        user.setPassword(null);
        user.setUpdateTime(System.currentTimeMillis());
        // 变更前
        User userFromDB = userMapper.selectByPrimaryKey(user.getId());
        // last organization id 变了
        if (user.getLastOrganizationId() != null && !StringUtils.equals(user.getLastOrganizationId(), userFromDB.getLastOrganizationId())
                && !baseUserMapper.isSuperUser(user.getId())) {
            List<Project> projects = getProjectListByWsAndUserId(user.getId(), user.getLastOrganizationId());
            if (!projects.isEmpty()) {
                // 如果传入的 last_project_id 是 last_organization_id 下面的
                boolean present = projects.stream().anyMatch(p -> StringUtils.equals(p.getId(), user.getLastProjectId()));
                if (!present) {
                    user.setLastProjectId(projects.getFirst().getId());
                }
            } else {
                user.setLastProjectId(StringUtils.EMPTY);
            }
        }
        // 执行变更
        userMapper.updateByPrimaryKeySelective(user);
        if (BooleanUtils.isFalse(user.getEnable())) {
            SessionUtils.kickOutUser(user.getId());
        }
    }

    private List<Project> getProjectListByWsAndUserId(String userId, String organizationId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(organizationId).andEnableEqualTo(true);
        List<Project> projects = projectMapper.selectByExample(projectExample);

        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        List<Project> projectList = new ArrayList<>();
        userRoleRelations.forEach(userRoleRelation -> projects.forEach(project -> {
            if (StringUtils.equals(userRoleRelation.getSourceId(), project.getId())) {
                if (!projectList.contains(project)) {
                    projectList.add(project);
                }
            }
        }));
        return projectList;
    }


    public UserDTO getUserDTOByEmail(String email, String... source) {
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(email);

        if (!CollectionUtils.isEmpty(Arrays.asList(source))) {
            criteria.andSourceIn(Arrays.asList(source));
        }

        List<User> users = userMapper.selectByExample(example);

        if (users == null || users.size() == 0) {
            return null;
        }

        return getUserDTO(users.getFirst().getId());
    }

    public boolean checkUserPassword(String userId, String password) {
        if (StringUtils.isBlank(userId)) {
            throw new MSException(Translator.get("user_name_is_null"));
        }
        if (StringUtils.isBlank(password)) {
            throw new MSException(Translator.get("password_is_null"));
        }
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andPasswordEqualTo(CodingUtils.md5(password));
        return userMapper.countByExample(example) > 0;
    }

    private UserRolePermissionDTO getUserRolePermission(String userId) {
        UserRolePermissionDTO permissionDTO = new UserRolePermissionDTO();
        List<UserRoleResourceDTO> list = new ArrayList<>();
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            return permissionDTO;
        }
        permissionDTO.setUserRoleRelations(userRoleRelations);
        List<String> roleList = userRoleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andIdIn(roleList);
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        permissionDTO.setUserRoles(userRoles);
        for (UserRole gp : userRoles) {
            UserRoleResourceDTO dto = new UserRoleResourceDTO();
            dto.setUserRole(gp);
            UserRolePermissionExample userRolePermissionExample = new UserRolePermissionExample();
            userRolePermissionExample.createCriteria().andRoleIdEqualTo(gp.getId());
            List<UserRolePermission> userRolePermissions = userRolePermissionMapper.selectByExample(userRolePermissionExample);
            dto.setUserRolePermissions(userRolePermissions);
            list.add(dto);
        }
        permissionDTO.setList(list);
        return permissionDTO;
    }

    public boolean checkWhetherChangePasswordOrNot(LoginRequest request) {
        // 升级之后 admin 还使用弱密码也提示修改
        if (StringUtils.equals("admin", request.getUsername())) {
            UserExample example = new UserExample();
            example.createCriteria().andIdEqualTo("admin")
                    .andPasswordEqualTo(CodingUtils.md5("metersphere"));
            return userMapper.countByExample(example) > 0;
        }

        return false;
    }

    public List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(String keyword) {
        return baseUserMapper.getExcludeSelectOptionWithLimit(keyword);
    }

    public List<OptionDTO> getSelectOptionByIdsWithDeleted(List<String> ids) {
        return baseUserMapper.getSelectOptionByIdsWithDeleted(ids);
    }

    /**
     * 根据用户ID列表，获取用户
     *
     * @param userIds
     * @return
     */
    public Map<String, String> getUserNameMap(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return getSelectOptionByIdsWithDeleted(userIds)
                .stream()
                .collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
    }
}
