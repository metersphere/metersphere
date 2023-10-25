package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.UserExtendDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.*;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.*;
import io.metersphere.system.request.ProjectAddMemberBatchRequest;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CommonProjectService {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserRolePermissionMapper userRolePermissionMapper;
    @Resource
    private UserLoginService userLoginService;
    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourcePoolOrganizationMapper testResourcePoolOrganizationMapper;
    @Resource
    private ProjectTestResourcePoolMapper projectTestResourcePoolMapper;

    @Autowired
    public CommonProjectService(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public ProjectDTO get(String id) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andIdEqualTo(id).andEnableEqualTo(true);
        List<Project> project = projectMapper.selectByExample(example);
        ProjectDTO projectDTO = new ProjectDTO();
        if (CollectionUtils.isNotEmpty(project)) {
            BeanUtils.copyBean(projectDTO, project.get(0));
            projectDTO.setOrganizationName(organizationMapper.selectByPrimaryKey(projectDTO.getOrganizationId()).getName());
            List<ProjectDTO> projectDTOS = buildUserInfo(List.of(projectDTO));
            projectDTO = projectDTOS.get(0);
        } else {
            return null;
        }
        return projectDTO;
    }

    /**
     * @param addProjectDTO 添加项目的时候  默认给用户组添加管理员的权限
     * @param createUser
     * @param path          请求路径
     * @param module        日志记录模块
     * @return
     */
    public ProjectDTO add(AddProjectRequest addProjectDTO, String createUser, String path, String module) {

        Project project = new Project();
        ProjectDTO projectDTO = new ProjectDTO();
        project.setId(IDGenerator.nextStr());
        project.setName(addProjectDTO.getName());
        project.setOrganizationId(addProjectDTO.getOrganizationId());
        checkProjectExistByName(project);
        project.setCreateTime(System.currentTimeMillis());
        project.setUpdateTime(System.currentTimeMillis());
        project.setUpdateUser(createUser);
        project.setCreateUser(createUser);
        project.setEnable(addProjectDTO.getEnable());
        project.setDescription(addProjectDTO.getDescription());
        addProjectDTO.setId(project.getId());
        BeanUtils.copyBean(projectDTO, project);
        projectDTO.setOrganizationName(organizationMapper.selectByPrimaryKey(project.getOrganizationId()).getName());
        //判断是否有模块设置
        if (CollectionUtils.isNotEmpty(addProjectDTO.getModuleIds())) {
            project.setModuleSetting(JSON.toJSONString(addProjectDTO.getModuleIds()));
            projectDTO.setModuleIds(addProjectDTO.getModuleIds());
        }

        ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
        memberRequest.setProjectIds(List.of(project.getId()));
        if (CollectionUtils.isEmpty(addProjectDTO.getUserIds())) {
            memberRequest.setUserIds(List.of(createUser));
        } else {
            memberRequest.setUserIds(addProjectDTO.getUserIds());
        } //资源池
        if (CollectionUtils.isNotEmpty(addProjectDTO.getResourcePoolIds())) {
            checkResourcePoolExist(addProjectDTO.getResourcePoolIds());
            List<ProjectTestResourcePool> projectTestResourcePools = new ArrayList<>();
            ProjectTestResourcePoolExample projectTestResourcePoolExample = new ProjectTestResourcePoolExample();
            projectTestResourcePoolExample.createCriteria().andProjectIdEqualTo(project.getId());
            projectTestResourcePoolMapper.deleteByExample(projectTestResourcePoolExample);
            addProjectDTO.getResourcePoolIds().forEach(resourcePoolId -> {
                ProjectTestResourcePool projectTestResourcePool = new ProjectTestResourcePool();
                projectTestResourcePool.setProjectId(project.getId());
                projectTestResourcePool.setTestResourcePoolId(resourcePoolId);
                projectTestResourcePools.add(projectTestResourcePool);
            });
            projectTestResourcePoolMapper.batchInsert(projectTestResourcePools);
        }
        projectMapper.insertSelective(project);
        serviceInvoker.invokeCreateServices(project.getId());
        //添加项目管理员   创建的时候如果没有传管理员id  则默认创建者为管理员
        this.addProjectAdmin(memberRequest, createUser, path,
                OperationLogType.ADD.name(), Translator.get("add"), module);
        return projectDTO;
    }

    /**
     * 检查添加的人员是否存在组织中  判断传过来的用户id是否在组织下，如果不存在，给用户创建一个组织成员的身份
     *
     * @param
     */
    public void checkOrgRoleExit(List<String> userId, String orgId, String createUser, Map<String, String> nameMap, String path, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdIn(userId).andSourceIdEqualTo(orgId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //把用户id放到一个新的list
        List<String> orgUserIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
        if (CollectionUtils.isNotEmpty(userId)) {
            List<UserRoleRelation> userRoleRelation = new ArrayList<>();
            userId.forEach(id -> {
                if (!orgUserIds.contains(id)) {
                    UserRoleRelation memberRole = new UserRoleRelation();
                    memberRole.setId(IDGenerator.nextStr());
                    memberRole.setUserId(id);
                    memberRole.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
                    memberRole.setSourceId(orgId);
                    memberRole.setCreateTime(System.currentTimeMillis());
                    memberRole.setCreateUser(createUser);
                    memberRole.setOrganizationId(orgId);
                    userRoleRelation.add(memberRole);
                    LogDTO logDTO = new LogDTO(orgId, orgId, memberRole.getId(), createUser, OperationLogType.ADD.name(), module, Translator.get("add") + Translator.get("organization_member") + ": " + nameMap.get(id));
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
            if (CollectionUtils.isNotEmpty(userRoleRelation)) {
                userRoleRelationMapper.batchInsert(userRoleRelation);
            }

        }
        operationLogService.batchAdd(logDTOList);
    }

    private void checkProjectExistByName(Project project) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andNameEqualTo(project.getName()).andOrganizationIdEqualTo(project.getOrganizationId()).andIdNotEqualTo(project.getId());
        if (projectMapper.selectByExample(example).size() > 0) {
            throw new MSException(Translator.get("project_name_already_exists"));
        }
    }

    /**
     * 检查项目是否存在
     *
     * @param id
     */
    public void checkProjectNotExist(String id) {
        if (projectMapper.selectByPrimaryKey(id) == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }

    public List<ProjectDTO> buildUserInfo(List<ProjectDTO> projectList) {
        //取项目的创建人  修改人  删除人到一个list中
        List<String> userIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(projectList)) {
            userIds.addAll(projectList.stream().map(ProjectDTO::getCreateUser).toList());
            userIds.addAll(projectList.stream().map(ProjectDTO::getUpdateUser).toList());
            userIds.addAll(projectList.stream().map(ProjectDTO::getDeleteUser).toList());
            Map<String, String> userMap = userLoginService.getUserNameMap(userIds.stream().filter(StringUtils::isNotBlank).distinct().toList());
            // 获取项目id
            List<String> projectIds = projectList.stream().map(ProjectDTO::getId).toList();
            List<UserExtendDTO> users = extSystemProjectMapper.getProjectAdminList(projectIds);
            List<ProjectDTO> projectDTOList = extSystemProjectMapper.getProjectExtendDTOList(projectIds);
            Map<String, ProjectDTO> projectMap = projectDTOList.stream().collect(Collectors.toMap(ProjectDTO::getId, projectDTO -> projectDTO));
            //根据sourceId分组
            Map<String, List<UserExtendDTO>> userMapList = users.stream().collect(Collectors.groupingBy(UserExtendDTO::getSourceId));
            //获取资源池
            List<ProjectResourcePoolDTO> projectResourcePoolDTOList = extSystemProjectMapper.getProjectResourcePoolDTOList(projectIds);
            //根据projectId分组 key为项目id 值为资源池TestResourcePool
            Map<String, List<ProjectResourcePoolDTO>> poolMap = projectResourcePoolDTOList.stream().collect(Collectors.groupingBy(ProjectResourcePoolDTO::getProjectId));

            projectList.forEach(projectDTO -> {
                if (StringUtils.isNotBlank(projectDTO.getModuleSetting())) {
                    projectDTO.setModuleIds(JSON.parseArray(projectDTO.getModuleSetting(), String.class));
                }
                projectDTO.setMemberCount(projectMap.get(projectDTO.getId()).getMemberCount());
                List<UserExtendDTO> userExtendDTOS = userMapList.get(projectDTO.getId());
                if (CollectionUtils.isNotEmpty(userExtendDTOS)) {
                    projectDTO.setAdminList(userExtendDTOS);
                    List<String> userIdList = userExtendDTOS.stream().map(User::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(userIdList) && userIdList.contains(projectDTO.getCreateUser())) {
                        projectDTO.setProjectCreateUserIsAdmin(true);
                    } else {
                        projectDTO.setProjectCreateUserIsAdmin(false);
                    }
                } else {
                    projectDTO.setAdminList(new ArrayList<>());
                }
                List<ProjectResourcePoolDTO> projectResourcePoolDTOS = poolMap.get(projectDTO.getId());
                if (CollectionUtils.isNotEmpty(projectResourcePoolDTOS)) {
                    projectDTO.setResourcePoolList(projectResourcePoolDTOS);
                } else {
                    projectDTO.setResourcePoolList(new ArrayList<>());
                }
                projectDTO.setCreateUser(userMap.get(projectDTO.getCreateUser()));
                projectDTO.setUpdateUser(userMap.get(projectDTO.getUpdateUser()));
                projectDTO.setDeleteUser(userMap.get(projectDTO.getDeleteUser()));
            });
        }
        return projectList;
    }

    public ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser, String path, String module) {
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
        projectDTO.setOrganizationName(organizationMapper.selectByPrimaryKey(project.getOrganizationId()).getName());
        BeanUtils.copyBean(projectDTO, project);
        //资源池
        if (CollectionUtils.isNotEmpty(updateProjectDto.getResourcePoolIds())) {
            checkResourcePoolExist(updateProjectDto.getResourcePoolIds());
            List<ProjectTestResourcePool> projectTestResourcePools = new ArrayList<>();
            ProjectTestResourcePoolExample projectTestResourcePoolExample = new ProjectTestResourcePoolExample();
            projectTestResourcePoolExample.createCriteria().andProjectIdEqualTo(project.getId());
            projectTestResourcePoolMapper.deleteByExample(projectTestResourcePoolExample);
            updateProjectDto.getResourcePoolIds().forEach(resourcePoolId -> {
                ProjectTestResourcePool projectTestResourcePool = new ProjectTestResourcePool();
                projectTestResourcePool.setProjectId(project.getId());
                projectTestResourcePool.setTestResourcePoolId(resourcePoolId);
                projectTestResourcePools.add(projectTestResourcePool);
            });
            projectTestResourcePoolMapper.batchInsert(projectTestResourcePools);
        } else {
            ProjectTestResourcePoolExample projectTestResourcePoolExample = new ProjectTestResourcePoolExample();
            projectTestResourcePoolExample.createCriteria().andProjectIdEqualTo(project.getId());
            projectTestResourcePoolMapper.deleteByExample(projectTestResourcePoolExample);
        }

        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(project.getId()).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        List<String> orgUserIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(updateProjectDto.getUserIds())) {
            //updateProjectDto.getUserIds() 为前端传过来的用户id  与数据库中的用户id做对比  如果数据库中的用户id不在前端传过来的用户id中  则删除
            List<String> deleteIds = orgUserIds.stream()
                    .filter(item -> !updateProjectDto.getUserIds().contains(item))
                    .collect(Collectors.toList());

            List<String> insertIds = updateProjectDto.getUserIds().stream()
                    .filter(item -> !orgUserIds.contains(item))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                UserRoleRelationExample deleteExample = new UserRoleRelationExample();
                deleteExample.createCriteria().andSourceIdEqualTo(project.getId()).andUserIdIn(deleteIds).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
                userRoleRelationMapper.selectByExample(deleteExample).forEach(userRoleRelation -> {
                    User user = userMapper.selectByPrimaryKey(userRoleRelation.getUserId());
                    LogDTO logDTO = new LogDTO(project.getId(), project.getOrganizationId(), userRoleRelation.getId(), updateUser, OperationLogType.DELETE.name(), module, Translator.get("delete") + Translator.get("project_admin") + ": " + user.getName());
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                });
                userRoleRelationMapper.deleteByExample(deleteExample);
            }
            if (CollectionUtils.isNotEmpty(insertIds)) {
                ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
                memberRequest.setProjectIds(List.of(project.getId()));
                memberRequest.setUserIds(insertIds);
                this.addProjectAdmin(memberRequest, updateUser, path, OperationLogType.ADD.name(),
                        Translator.get("add"), module);
            }
        } else {
            if (CollectionUtils.isNotEmpty(orgUserIds)) {
                //如果前端传过来的用户id为空  则删除项目所有管理员
                UserRoleRelationExample deleteExample = new UserRoleRelationExample();
                deleteExample.createCriteria().andSourceIdEqualTo(project.getId()).andUserIdIn(orgUserIds).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
                userRoleRelationMapper.selectByExample(deleteExample).forEach(userRoleRelation -> {
                    User user = userMapper.selectByPrimaryKey(userRoleRelation.getUserId());
                    LogDTO logDTO = new LogDTO(project.getId(), project.getOrganizationId(), userRoleRelation.getId(), updateUser, OperationLogType.DELETE.name(), module, Translator.get("delete") + Translator.get("project_admin") + ": " + user.getName());
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                });
                userRoleRelationMapper.deleteByExample(deleteExample);
            }
        }
        if (CollectionUtils.isNotEmpty(logDTOList)) {
            operationLogService.batchAdd(logDTOList);
        }
        //判断是否有模块设置
        if (CollectionUtils.isNotEmpty(updateProjectDto.getModuleIds())) {
            project.setModuleSetting(JSON.toJSONString(updateProjectDto.getModuleIds()));
            projectDTO.setModuleIds(updateProjectDto.getModuleIds());
        } else {
            project.setModuleSetting(null);
            projectDTO.setModuleIds(new ArrayList<>());
        }

        projectMapper.updateByPrimaryKeySelective(project);
        return projectDTO;
    }

    public void checkResourcePoolExist(List<String> poolIds) {
        TestResourcePoolExample testResourcePoolExample = new TestResourcePoolExample();
        testResourcePoolExample.createCriteria().andIdIn(poolIds).andEnableEqualTo(true).andDeletedEqualTo(false);
        List<TestResourcePool> testResourcePools = testResourcePoolMapper.selectByExample(testResourcePoolExample);
        if (poolIds.size() != testResourcePools.size()) {
            throw new MSException(Translator.get("resource_pool_not_exist"));
        }
    }

    public int delete(String id, String deleteUser) {
        //TODO  删除项目删除全部资源 这里的删除只是假删除
        checkProjectNotExist(id);
        Project project = new Project();
        project.setId(id);
        project.setDeleteUser(deleteUser);
        project.setDeleted(true);
        project.setDeleteTime(System.currentTimeMillis());
        return projectMapper.updateByPrimaryKeySelective(project);
    }

    /**
     * 添加项目管理员
     *
     * @param request
     * @param createUser 创建人
     * @param path       请求路径
     * @param type       操作类型
     * @param content    操作内容
     * @param module     日志记录模块
     */
    public void addProjectAdmin(ProjectAddMemberBatchRequest request, String createUser, String path, String type,
                                String content, String module) {

        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            Map<String, String> nameMap = addUserPre(request, createUser, path, module, projectId, project);
            request.getUserIds().forEach(userId -> {
                UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
                userRoleRelationExample.createCriteria().andUserIdEqualTo(userId)
                        .andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
                if (userRoleRelationMapper.selectByExample(userRoleRelationExample).size() == 0) {
                    UserRoleRelation adminRole = new UserRoleRelation();
                    adminRole.setId(IDGenerator.nextStr());
                    adminRole.setUserId(userId);
                    adminRole.setRoleId(InternalUserRole.PROJECT_ADMIN.getValue());
                    adminRole.setSourceId(projectId);
                    adminRole.setCreateTime(System.currentTimeMillis());
                    adminRole.setCreateUser(createUser);
                    adminRole.setOrganizationId(project.getOrganizationId());
                    userRoleRelations.add(adminRole);
                    LogDTO logDTO = new LogDTO(projectId, project.getOrganizationId(), adminRole.getId(), createUser, type, module, content + Translator.get("project_admin") + ": " + nameMap.get(userId));
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.batchInsert(userRoleRelations);
        }
        operationLogService.batchAdd(logDTOList);
    }

    private Map<String, String> addUserPre(ProjectAddMemberBatchRequest request, String createUser, String path, String module, String projectId, Project project) {
        checkProjectNotExist(projectId);
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(request.getUserIds()).andDeletedEqualTo(false);
        List<User> users = userMapper.selectByExample(userExample);
        if (request.getUserIds().size() != users.size()) {
            throw new MSException(Translator.get("user_not_exist"));
        }
        //把id和名称放一个map中
        Map<String, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getName));
        this.checkOrgRoleExit(request.getUserIds(), project.getOrganizationId(), createUser, userMap, path, module);
        return userMap;
    }

    /**
     * 添加项目成员
     *
     * @param request
     * @param createUser 创建人
     * @param path       请求路径
     * @param type       操作类型
     * @param content    操作内容
     * @param module     日志记录模块
     */
    public void addProjectMember(ProjectAddMemberBatchRequest request, String createUser, String path, String type, String content, String module) {

        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            Map<String, String> userMap = addUserPre(request, createUser, path, module, projectId, project);
            request.getUserIds().forEach(userId -> {
                UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
                userRoleRelationExample.createCriteria().andUserIdEqualTo(userId)
                        .andSourceIdEqualTo(projectId);
                if (userRoleRelationMapper.selectByExample(userRoleRelationExample).size() == 0) {
                    UserRoleRelation memberRole = new UserRoleRelation();
                    memberRole.setId(IDGenerator.nextStr());
                    memberRole.setUserId(userId);
                    memberRole.setRoleId(InternalUserRole.PROJECT_MEMBER.getValue());
                    memberRole.setSourceId(projectId);
                    memberRole.setCreateTime(System.currentTimeMillis());
                    memberRole.setCreateUser(createUser);
                    memberRole.setOrganizationId(project.getOrganizationId());
                    userRoleRelations.add(memberRole);
                    LogDTO logDTO = new LogDTO(OperationLogConstants.SYSTEM, OperationLogConstants.SYSTEM, memberRole.getId(), createUser, type, module, content + Translator.get("project_member") + ": " + userMap.get(userId));
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.batchInsert(userRoleRelations);
        }
        operationLogService.batchAdd(logDTOList);
    }

    public int removeProjectMember(String projectId, String userId, String createUser, String module, String path) {
        checkProjectNotExist(projectId);
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdEqualTo(userId).andDeletedEqualTo(false);
        List<User> users = userMapper.selectByExample(userExample);
        User user = CollectionUtils.isNotEmpty(users) ? users.get(0) : null;
        if (user == null) {
            throw new MSException(Translator.get("user_not_exist"));
        }
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId)
                .andSourceIdEqualTo(projectId);
        if (StringUtils.equals(projectId, user.getLastProjectId())) {
            user.setLastProjectId(StringUtils.EMPTY);
            userMapper.updateByPrimaryKeySelective(user);
        }
        List<LogDTO> logDTOList = new ArrayList<>();
        userRoleRelationMapper.selectByExample(userRoleRelationExample).forEach(userRoleRelation -> {
            LogDTO logDTO = new LogDTO(OperationLogConstants.SYSTEM, OperationLogConstants.SYSTEM, userRoleRelation.getId(), createUser, OperationLogType.DELETE.name(), module, Translator.get("delete") + Translator.get("project_member") + ": " + user.getName());
            setLog(logDTO, path, HttpMethodConstants.GET.name(), logDTOList);
        });
        operationLogService.batchAdd(logDTOList);
        return userRoleRelationMapper.deleteByExample(userRoleRelationExample);
    }

    public int revoke(String id, String updateUser) {
        checkProjectNotExist(id);
        Project project = new Project();
        project.setId(id);
        project.setDeleted(false);
        project.setDeleteTime(null);
        project.setDeleteUser(null);
        project.setUpdateUser(updateUser);
        project.setUpdateTime(System.currentTimeMillis());
        return projectMapper.updateByPrimaryKeySelective(project);
    }

    /**
     * 删除项目   一般是定时任务会触发
     *
     * @param projects
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteProject(List<Project> projects) {
        // 删除项目
        List<LogDTO> logDTOList = new ArrayList<>();
        projects.forEach(project -> {
            serviceInvoker.invokeServices(project.getId());
            LogUtils.info("send delete_project message, project id: " + project.getId());

            deleteProjectUserGroup(project.getId());
            //删除资源池关联表
            ProjectTestResourcePoolExample projectTestResourcePoolExample = new ProjectTestResourcePoolExample();
            projectTestResourcePoolExample.createCriteria().andProjectIdEqualTo(project.getId());
            projectTestResourcePoolMapper.deleteByExample(projectTestResourcePoolExample);
            // delete project
            projectMapper.deleteByPrimaryKey(project.getId());
            LogDTO logDTO = new LogDTO(OperationLogConstants.SYSTEM, project.getOrganizationId(), project.getId(), Translator.get("scheduled_tasks"), OperationLogType.DELETE.name(), OperationLogModule.SETTING_ORGANIZATION_PROJECT, Translator.get("delete") + Translator.get("project") + ": " + project.getName());
            setLog(logDTO, StringUtils.EMPTY, StringUtils.EMPTY, logDTOList);
        });
        operationLogService.batchAdd(logDTOList);
    }

    /**
     * 删除自定义用户组和权限关系表、项目和用户关系数据
     *
     * @param projectId
     */
    private void deleteProjectUserGroup(String projectId) {
        UserRoleRelationExample userGroupExample = new UserRoleRelationExample();
        userGroupExample.createCriteria().andSourceIdEqualTo(projectId);
        userRoleRelationMapper.deleteByExample(userGroupExample);
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andScopeIdEqualTo(projectId).andTypeEqualTo(UserRoleType.PROJECT.name());
        List<UserRole> roles = userRoleMapper.selectByExample(userRoleExample);
        if (CollectionUtils.isNotEmpty(roles)) {
            List<String> roleIds = roles.stream().map(UserRole::getId).collect(Collectors.toList());
            UserRolePermissionExample userRolePermissionExample = new UserRolePermissionExample();
            userRolePermissionExample.createCriteria().andRoleIdIn(roleIds);
            userRolePermissionMapper.deleteByExample(userRolePermissionExample);
            userRoleMapper.deleteByExample(userRoleExample);
        }
    }

    /**
     * @param path       请求路径
     * @param method     请求方法
     * @param logDTOList 日志集合
     */
    private void setLog(LogDTO dto, String path, String method, List<LogDTO> logDTOList) {
        dto.setPath(path);
        dto.setMethod(method);
        dto.setOriginalValue(JSON.toJSONBytes(StringUtils.EMPTY));
        logDTOList.add(dto);
    }

    public void enable(String id, String updateUser) {
        checkProjectNotExist(id);
        Project project = new Project();
        project.setId(id);
        project.setEnable(true);
        project.setUpdateUser(updateUser);
        project.setUpdateTime(System.currentTimeMillis());
        projectMapper.updateByPrimaryKeySelective(project);
    }

    public void disable(String id, String updateUser) {
        checkProjectNotExist(id);
        Project project = new Project();
        project.setId(id);
        project.setEnable(false);
        project.setUpdateUser(updateUser);
        project.setUpdateTime(System.currentTimeMillis());
        projectMapper.updateByPrimaryKeySelective(project);
    }

    public List<OptionDTO> getTestResourcePoolOptions(String organizationId) {
        //获取制定组织的资源池  和全部组织的资源池
        List<TestResourcePool> testResourcePools = new ArrayList<>();
        if (StringUtils.isNotBlank(organizationId)) {
            TestResourcePoolOrganizationExample example = new TestResourcePoolOrganizationExample();
            example.createCriteria().andOrgIdEqualTo(organizationId);
            List<TestResourcePoolOrganization> orgPools = testResourcePoolOrganizationMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(orgPools)) {
                List<String> poolIds = orgPools.stream().map(TestResourcePoolOrganization::getTestResourcePoolId).toList();
                TestResourcePoolExample poolExample = new TestResourcePoolExample();
                poolExample.createCriteria().andIdIn(poolIds).andEnableEqualTo(true).andDeletedEqualTo(false);
                testResourcePools.addAll(testResourcePoolMapper.selectByExample(poolExample));
            }
        }
        //获取应用全部组织的资源池
        TestResourcePoolExample poolExample = new TestResourcePoolExample();
        poolExample.createCriteria().andAllOrgEqualTo(true).andEnableEqualTo(true).andDeletedEqualTo(false);
        testResourcePools.addAll(testResourcePoolMapper.selectByExample(poolExample));

        testResourcePools = testResourcePools.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        return testResourcePools.stream().map(testResourcePool ->
                new OptionDTO(testResourcePool.getId(), testResourcePool.getName())
        ).toList();
    }

    public void rename(UpdateProjectNameRequest request, String userId) {
        checkProjectNotExist(request.getId());
        Project project = new Project();
        project.setId(request.getId());
        project.setName(request.getName());
        project.setOrganizationId(request.getOrganizationId());
        checkProjectExistByName(project);
        project.setCreateTime(null);
        project.setCreateUser(null);
        project.setUpdateUser(userId);
        project.setUpdateTime(System.currentTimeMillis());
        projectMapper.updateByPrimaryKeySelective(project);
    }
}
