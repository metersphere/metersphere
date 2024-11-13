package io.metersphere.project.service;

import io.metersphere.project.constants.ProjectMenuConstants;
import io.metersphere.project.domain.*;
import io.metersphere.project.dto.ProjectRequest;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.project.mapper.ProjectVersionMapper;
import io.metersphere.project.request.ProjectSwitchRequest;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolBlob;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.taskhub.ResourcePoolOptionsDTO;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.service.CommonProjectPoolService;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.utils.ServiceUtils;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private ExtProjectMapper extProjectMapper;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private CommonProjectPoolService commonProjectPoolService;
    @Resource
    private ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;
    @Resource
    private BaseUserMapper baseUserMapper;

    public static final Long ORDER_STEP = 5000L;
    @Resource
    private BaseTaskHubService baseTaskHubService;


    public List<Project> getUserProject(String organizationId, String userId) {
        checkOrg(organizationId);
        //查询用户当前的项目  如果存在默认排在第一个
        User user = baseUserMapper.selectById(userId);
        String projectId;
        if (user != null && StringUtils.isNotBlank(user.getLastProjectId())) {
            projectId = user.getLastProjectId();
        } else {
            projectId = null;
        }
        //判断用户是否是系统管理员
        List<Project> allProject = new ArrayList<>();
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId).andRoleIdEqualTo(InternalUserRole.ADMIN.name());
        if (userRoleRelationMapper.countByExample(userRoleRelationExample) > 0) {
            allProject = extProjectMapper.getAllProject(organizationId);
        } else {
            allProject = extProjectMapper.getUserProject(organizationId, userId);
        }
        List<Project> temp = allProject;
        return allProject.stream()
                .filter(project -> StringUtils.equals(project.getId(), projectId))
                .findFirst()
                .map(project -> {
                    temp.remove(project);
                    temp.add(0, project);
                    return temp;
                })
                .orElse(allProject);
    }

    private void checkOrg(String organizationId) {
        if (organizationMapper.selectByPrimaryKey(organizationId) == null) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
    }

    public UserDTO switchProject(ProjectSwitchRequest request, String currentUserId) {
        if (!StringUtils.equals(currentUserId, request.getUserId())) {
            throw new MSException(Translator.get("not_authorized"));
        }
        if (projectMapper.selectByPrimaryKey(request.getProjectId()) == null) {
            throw new MSException(Translator.get("project_not_exist"));
        }

        User user = new User();
        user.setId(request.getUserId());
        user.setLastProjectId(request.getProjectId());
        userLoginService.updateUser(user);
        UserDTO userDTO = userLoginService.getUserDTO(user.getId());
        SessionUser sessionUser = SessionUser.fromUser(userDTO, SessionUtils.getSessionId());
        SessionUtils.putUser(sessionUser);
        return sessionUser;
    }

    public ProjectDTO getProjectById(String id) {
        return commonProjectService.get(id);
    }

    public ProjectDTO update(ProjectRequest updateProjectDto, String updateUser) {
        Project project = new Project();
        ProjectDTO projectDTO = new ProjectDTO();
        project.setId(updateProjectDto.getId());
        project.setName(updateProjectDto.getName());
        project.setDescription(updateProjectDto.getDescription());
        project.setOrganizationId(updateProjectDto.getOrganizationId());
        project.setEnable(updateProjectDto.getEnable());
        project.setUpdateUser(updateUser);
        project.setCreateUser(null);
        project.setCreateTime(null);
        project.setUpdateTime(System.currentTimeMillis());
        checkProjectExistByName(project);
        checkProjectNotExist(project.getId());
        projectDTO.setOrganizationName(organizationMapper.selectByPrimaryKey(updateProjectDto.getOrganizationId()).getName());
        BeanUtils.copyBean(projectDTO, project);

        projectMapper.updateByPrimaryKeySelective(project);
        return projectDTO;
    }

    private void checkProjectExistByName(Project project) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andNameEqualTo(project.getName()).andOrganizationIdEqualTo(project.getOrganizationId()).andIdNotEqualTo(project.getId());
        if (projectMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("project_name_already_exists"));
        }
    }

    public Project checkProjectNotExist(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
        return project;
    }

    private List<String> getPoolIds(String projectId) {
        ProjectTestResourcePoolExample example = new ProjectTestResourcePoolExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectTestResourcePool> projectPools = projectTestResourcePoolMapper.selectByExample(example);
        return projectPools.stream().map(ProjectTestResourcePool::getTestResourcePoolId).toList();
    }

    public List<OptionDTO> getPoolOptions(String projectId) {
        List<TestResourcePool> pools = getPoolOption(projectId);
        return pools.stream().map(pool -> {
            OptionDTO option = new OptionDTO();
            option.setId(pool.getId());
            option.setName(pool.getName());
            return option;
        }).toList();
    }

    public static Project checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(Objects.requireNonNull(CommonBeanFactory.getBean(ProjectMapper.class)).selectByPrimaryKey(id), "permission.project.name");
    }

    /**
     * 获取指定项目的最新版本
     *
     * @param projectId 项目ID
     * @return 最新版本
     */
    public ProjectVersion getLatestVersion(String projectId) {
        ProjectVersionExample projectVersionExample = new ProjectVersionExample();
        projectVersionExample.createCriteria().andProjectIdEqualTo(projectId);
        return projectVersionMapper.selectByExample(projectVersionExample).getFirst();
    }

    public Long getNextOrder(Function<String, Long> getLastPosFunc, String projectId) {
        Long pos = getLastPosFunc.apply(projectId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    public boolean hasPermission(String id, String userId) {
        boolean hasPermission = true;
        //判断用户是否是系统管理员
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId).andRoleIdEqualTo(InternalUserRole.ADMIN.name());
        if (userRoleRelationMapper.countByExample(userRoleRelationExample) > 0) {
            return hasPermission;
        }
        ProjectExample example = new ProjectExample();
        example.createCriteria().andIdEqualTo(id).andEnableEqualTo(true);
        if (CollectionUtils.isEmpty(projectMapper.selectByExample(example))) {
            return false;
        }
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(id);
        if (CollectionUtils.isEmpty(userRoleRelationMapper.selectByExample(userRoleRelationExample))) {
            return false;
        }
        return hasPermission;
    }

    public List<UserExtendDTO> getMemberOption(String projectId, String keyword) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return new ArrayList<>();
        }
        return extSystemProjectMapper.getMemberByProjectId(projectId, keyword);
    }

    public List<Project> getUserProjectWidthModule(String organizationId, String module, String userId) {
        if (StringUtils.isBlank(module)) {
            throw new MSException(Translator.get("module.name.is.empty"));
        }
        String moduleName = null;
        if (StringUtils.equalsIgnoreCase(module, "API") || StringUtils.equalsIgnoreCase(module, "SCENARIO")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_API_TEST;
        }
        if (StringUtils.equalsIgnoreCase(module, "FUNCTIONAL")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_FUNCTIONAL_CASE;
        }
        if (StringUtils.equalsIgnoreCase(module, "BUG")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_BUG;
        }
        if (StringUtils.equalsIgnoreCase(module, "TEST_PLAN")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_TEST_PLAN;
        }
        if (StringUtils.isBlank(moduleName)) {
            throw new MSException(Translator.get("module.name.is.error"));
        }
        checkOrg(organizationId);
        //查询用户当前的项目  如果存在默认排在第一个
        User user = baseUserMapper.selectById(userId);
        String projectId;
        if (user != null && StringUtils.isNotBlank(user.getLastProjectId())) {
            projectId = user.getLastProjectId();
        } else {
            projectId = null;
        }
        //判断用户是否是系统管理员
        List<Project> allProject;
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId).andRoleIdEqualTo(InternalUserRole.ADMIN.name());
        if (userRoleRelationMapper.countByExample(userRoleRelationExample) > 0) {
            allProject = extProjectMapper.getAllProjectWidthModule(organizationId, moduleName);
        } else {
            allProject = extProjectMapper.getUserProjectWidthModule(organizationId, userId, moduleName);
        }
        List<Project> temp = allProject;
        return allProject.stream()
                .filter(project -> StringUtils.equals(project.getId(), projectId))
                .findFirst()
                .map(project -> {
                    temp.remove(project);
                    temp.add(0, project);
                    return temp;
                })
                .orElse(allProject);

    }

    /**
     * 获取项目下可用的资源池
     *
     * @param projectId 项目ID
     * @return 资源池列表
     */
    public List<TestResourcePool> getPoolOption(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project.getAllResourcePool()) {
            return commonProjectPoolService.getProjectAllPoolsByEffect(project);
        } else {
            return extProjectMapper.getResourcePoolOption(projectId, "api_test");
        }
    }

    /**
     * 获取项目下的资源池及节点下拉选项
     *
     * @param projectId
     * @return
     */
    public List<ResourcePoolOptionsDTO> getProjectResourcePoolOptions(String projectId) {
        List<TestResourcePool> pools = getAllPoolOption(projectId);
        if (CollectionUtils.isNotEmpty(pools)) {
            Map<String, List<TestResourcePoolBlob>> poolMap = baseTaskHubService.getPoolMap(pools);
            return baseTaskHubService.handleOptions(pools, poolMap);
        }
        return null;
    }

    private List<TestResourcePool> getAllPoolOption(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project.getAllResourcePool()) {
            return commonProjectPoolService.getOrgTestResourcePools(project.getOrganizationId(), false);
        } else {
            return extProjectMapper.getResourcePoolOption(projectId, "api_test");
        }
    }

}

