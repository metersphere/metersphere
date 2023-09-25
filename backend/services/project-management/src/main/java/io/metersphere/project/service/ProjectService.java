package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.project.request.ProjectSwitchRequest;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.ModuleType;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.UpdateProjectRequest;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.mapper.TestResourcePoolOrganizationMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.BaseUserService;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.utils.ServiceUtils;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private ExtProjectMapper extProjectMapper;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
    @Resource
    private TestResourcePoolOrganizationMapper testResourcePoolOrganizationMapper;


    public List<Project> getUserProject(String organizationId, String userId) {
        if (organizationMapper.selectByPrimaryKey(organizationId) == null) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
        //判断用户是否是系统管理员
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId).andRoleIdEqualTo(InternalUserRole.ADMIN.name());
        List<UserRoleRelation> list = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        if (CollectionUtils.isNotEmpty(list)) {
            ProjectExample example = new ProjectExample();
            example.createCriteria().andOrganizationIdEqualTo(organizationId).andEnableEqualTo(true);
            return projectMapper.selectByExample(example);
        }
        return extProjectMapper.getUserProject(organizationId, userId);
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
        baseUserService.updateUser(user);
        UserDTO userDTO = baseUserService.getUserDTO(user.getId());
        SessionUser sessionUser = SessionUser.fromUser(userDTO, SessionUtils.getSessionId());
        SessionUtils.putUser(sessionUser);
        return sessionUser;
    }

    public ProjectDTO getProjectById(String id) {
        return commonProjectService.get(id);
    }

    public ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser) {
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
        //判断是否有模块设置
        if (CollectionUtils.isNotEmpty(updateProjectDto.getModuleIds())) {
            project.setModuleSetting(JSON.toJSONString(updateProjectDto.getModuleIds()));
            projectDTO.setModuleIds(updateProjectDto.getModuleIds());
        }

        projectMapper.updateByPrimaryKeySelective(project);
        return projectDTO;
    }

    private void checkProjectExistByName(Project project) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andNameEqualTo(project.getName()).andOrganizationIdEqualTo(project.getOrganizationId()).andIdNotEqualTo(project.getId());
        if (projectMapper.selectByExample(example).size() > 0) {
            throw new MSException(Translator.get("project_name_already_exists"));
        }
    }

    public void checkProjectNotExist(String id) {
        if (projectMapper.selectByPrimaryKey(id) == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }

    private List<String> getPoolIds(String projectId) {
        List<String> poolIds = new ArrayList<>();
        ProjectTestResourcePoolExample example = new ProjectTestResourcePoolExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectTestResourcePool> projectPools = projectTestResourcePoolMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(projectPools)) {
            return projectPools.stream().map(ProjectTestResourcePool::getTestResourcePoolId).toList();
        }
        //判断项目所属组织是否关联了资源池
        Project project = projectMapper.selectByPrimaryKey(projectId);
        TestResourcePoolOrganizationExample orgExample = new TestResourcePoolOrganizationExample();
        orgExample.createCriteria().andOrgIdEqualTo(project.getOrganizationId());
        List<TestResourcePoolOrganization> orgPools = testResourcePoolOrganizationMapper.selectByExample(orgExample);
        if (CollectionUtils.isNotEmpty(orgPools)) {
            poolIds.addAll(orgPools.stream().map(TestResourcePoolOrganization::getTestResourcePoolId).toList());
        }
        //获取应用全部组织的资源池
        TestResourcePoolExample poolExample = new TestResourcePoolExample();
        poolExample.createCriteria().andAllOrgEqualTo(true).andEnableEqualTo(true).andDeletedEqualTo(false);
        List<TestResourcePool> testResourcePools = testResourcePoolMapper.selectByExample(poolExample);
        poolIds.addAll(testResourcePools.stream().map(TestResourcePool::getId).toList());
        poolIds = poolIds.stream().distinct().filter(StringUtils::isNotBlank).toList();
        return poolIds;
    }

    public List<OptionDTO> getPoolOptions(String projectId, String type) {
        checkProjectNotExist(projectId);
        List<String> poolIds = getPoolIds(projectId);
        TestResourcePoolExample example = new TestResourcePoolExample();
        TestResourcePoolExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(poolIds).andEnableEqualTo(true).andDeletedEqualTo(false);
        List<TestResourcePool> testResourcePools = new ArrayList<>();
        testResourcePools =  switch (type) {
            case ModuleType.API_TEST-> {
                criteria.andApiTestEqualTo(true);
                yield testResourcePoolMapper.selectByExample(example);
            }
            case ModuleType.UI_TEST -> {
                criteria.andUiTestEqualTo(true);
                yield testResourcePoolMapper.selectByExample(example);
            }
            case ModuleType.LOAD_TEST -> {
                criteria.andLoadTestEqualTo(true);
                yield testResourcePoolMapper.selectByExample(example);
            }
            default -> new ArrayList<>();
        };
        return testResourcePools.stream().map(testResourcePool ->
            new OptionDTO(testResourcePool.getId(), testResourcePool.getName())
        ).toList();
    }

    public static Project checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(CommonBeanFactory.getBean(ProjectMapper.class).selectByPrimaryKey(id), "permission.project.name");
    }
}
