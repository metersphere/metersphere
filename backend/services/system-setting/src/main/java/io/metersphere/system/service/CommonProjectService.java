package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.dto.AddProjectRequest;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.dto.UpdateProjectRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.invoker.ProjectServiceInvoker;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.mapper.*;
import io.metersphere.system.request.ProjectAddMemberBatchRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    private final ProjectServiceInvoker serviceInvoker;

    @Autowired
    public CommonProjectService(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public Project get(String id) {
        return projectMapper.selectByPrimaryKey(id);
    }

    /**
     * @param addProjectDTO 添加项目的时候  默认给用户组添加管理员的权限
     * @param createUser
     * @param path 请求路径
     * @param module 日志记录模块
     * @return
     */
    public Project add(AddProjectRequest addProjectDTO, String createUser, String path, String module) {

        Project project = new Project();
        project.setId(UUID.randomUUID().toString());
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
        projectMapper.insertSelective(project);
        ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
        memberRequest.setProjectIds(List.of(project.getId()));
        if (CollectionUtils.isEmpty(addProjectDTO.getUserIds())) {
            memberRequest.setUserIds(List.of(createUser));
        } else {
            memberRequest.setUserIds(addProjectDTO.getUserIds());
        }
        //添加项目管理员   创建的时候如果没有传管理员id  则默认创建者为管理员
        this.addProjectAdmin(memberRequest, createUser, path,
                OperationLogType.ADD.name(), HttpMethodConstants.POST.name(), Translator.get("add"), module);
        return project;
    }

    /**
     * 检查添加的人员是否存在组织中
     *
     * @param
     */
    public void checkOrgRoleExit(String userId, String orgId, String createUser, String userName, String path, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(orgId);
        if (userRoleRelationMapper.selectByExample(userRoleRelationExample).size() == 0) {
            UserRoleRelation memberRole = new UserRoleRelation();
            memberRole.setId(UUID.randomUUID().toString());
            memberRole.setUserId(userId);
            memberRole.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
            memberRole.setSourceId(orgId);
            memberRole.setCreateTime(System.currentTimeMillis());
            memberRole.setCreateUser(createUser);
            userRoleRelationMapper.insert(memberRole);
            LogDTO logDTO = new LogDTO(orgId, orgId,memberRole.getId(), createUser, OperationLogType.ADD.name(), module,Translator.get("add") + Translator.get("organization_member") + ": " + userName);
            setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
        }
        operationLogService.batchAdd(logDTOList);
    }

    private void checkProjectExistByName(Project project) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andNameEqualTo(project.getName()).andOrganizationIdEqualTo(project.getOrganizationId());
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
        projectList.forEach(projectDTO -> {
            List<User> users = extSystemProjectMapper.getProjectAdminList(projectDTO.getId());
            projectDTO.setAdminList(users);
        });
        return projectList;
    }

    public Project update(UpdateProjectRequest updateProjectDto, String updateUser, String path, String module) {
        Project project = new Project();
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
                    LogDTO logDTO = new LogDTO(project.getId(), project.getOrganizationId(),userRoleRelation.getId(), updateUser, OperationLogType.DELETE.name(), module,Translator.get("delete") + Translator.get("project_admin") + ": " + user.getName());
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                });
                userRoleRelationMapper.deleteByExample(deleteExample);
            }
            if (CollectionUtils.isNotEmpty(insertIds)) {
                ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
                memberRequest.setProjectIds(List.of(project.getId()));
                memberRequest.setUserIds(insertIds);
                this.addProjectAdmin(memberRequest, updateUser, path, OperationLogType.UPDATE.name(),
                        HttpMethodConstants.POST.name(), Translator.get("update"), module);
            }
        } else {
            if (CollectionUtils.isNotEmpty(orgUserIds)) {
                //如果前端传过来的用户id为空  则删除项目所有管理员
                UserRoleRelationExample deleteExample = new UserRoleRelationExample();
                deleteExample.createCriteria().andSourceIdEqualTo(project.getId()).andUserIdIn(orgUserIds).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
                userRoleRelationMapper.selectByExample(deleteExample).forEach(userRoleRelation -> {
                    User user = userMapper.selectByPrimaryKey(userRoleRelation.getUserId());
                    LogDTO logDTO = new LogDTO(project.getId(), project.getOrganizationId(),userRoleRelation.getId(), updateUser, OperationLogType.DELETE.name(), module,Translator.get("delete") + Translator.get("project_admin") + ": " + user.getName());
                    setLog(logDTO, path, HttpMethodConstants.POST.name() ,logDTOList);
                });
                userRoleRelationMapper.deleteByExample(deleteExample);
            }
        }
        if (CollectionUtils.isNotEmpty(logDTOList)) {
            operationLogService.batchAdd(logDTOList);
        }

        projectMapper.updateByPrimaryKeySelective(project);
        return project;
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
     * @param request
     * @param createUser 创建人
     * @param path 请求路径
     * @param type 操作类型
     * @param method 请求方法
     * @param content 操作内容
     * @param module 日志记录模块
     */
    public void addProjectAdmin(ProjectAddMemberBatchRequest request, String createUser, String path, String type,
                                String method, String content, String module) {

        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            checkProjectNotExist(projectId);
            //判断传过来的用户id是否在组织下，如果不存在，给用户创建一个组织成员的身份
            request.getUserIds().forEach(userId -> {
                User user = userMapper.selectByPrimaryKey(userId);
                if (ObjectUtils.isEmpty(user)) {
                    throw new MSException(Translator.get("user_not_exist"));
                }
                Project project = projectMapper.selectByPrimaryKey(projectId);
                this.checkOrgRoleExit(userId, project.getOrganizationId(), createUser, user.getName(), path, module);
                UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
                userRoleRelationExample.createCriteria().andUserIdEqualTo(userId)
                        .andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_MEMBER.getValue());
                if (userRoleRelationMapper.selectByExample(userRoleRelationExample).size() == 0) {
                    UserRoleRelation adminRole = new UserRoleRelation();
                    adminRole.setId(UUID.randomUUID().toString());
                    adminRole.setUserId(userId);
                    adminRole.setRoleId(InternalUserRole.PROJECT_ADMIN.getValue());
                    adminRole.setSourceId(projectId);
                    adminRole.setCreateTime(System.currentTimeMillis());
                    adminRole.setCreateUser(createUser);
                    userRoleRelations.add(adminRole);
                    LogDTO logDTO = new LogDTO(projectId, project.getOrganizationId(), adminRole.getId(), createUser, type, module, content + Translator.get("project_admin") + ": " + user.getName());
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
        });
        userRoleRelationMapper.batchInsert(userRoleRelations);
        operationLogService.batchAdd(logDTOList);
    }

    /**
     * 添加项目成员
     * @param request
     * @param createUser 创建人
     * @param path 请求路径
     * @param type 操作类型
     * @param method 请求方法
     * @param content 操作内容
     * @param module 日志记录模块
     */
    public void addProjectMember(ProjectAddMemberBatchRequest request, String createUser, String path, String type,
                                 String method, String content, String module) {

        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            checkProjectNotExist(projectId);
            request.getUserIds().forEach(userId -> {
                User user = userMapper.selectByPrimaryKey(userId);
                if (ObjectUtils.isEmpty(user)) {
                    throw new MSException(Translator.get("user_not_exist"));
                }
                Project project = projectMapper.selectByPrimaryKey(projectId);
                this.checkOrgRoleExit(userId, project.getOrganizationId(), createUser, user.getName(), path, module);
                UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
                userRoleRelationExample.createCriteria().andUserIdEqualTo(userId)
                        .andSourceIdEqualTo(projectId);
                if (userRoleRelationMapper.selectByExample(userRoleRelationExample).size() == 0) {
                    UserRoleRelation memberRole = new UserRoleRelation();
                    memberRole.setId(UUID.randomUUID().toString());
                    memberRole.setUserId(userId);
                    memberRole.setRoleId(InternalUserRole.PROJECT_MEMBER.getValue());
                    memberRole.setSourceId(projectId);
                    memberRole.setCreateTime(System.currentTimeMillis());
                    memberRole.setCreateUser(createUser);
                    userRoleRelations.add(memberRole);
                    LogDTO logDTO = new LogDTO(projectId, project.getOrganizationId(),memberRole.getId(), createUser, type, module, content + Translator.get("project_member") + ": " + user.getName());
                    setLog(logDTO , path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
        });
        userRoleRelationMapper.batchInsert(userRoleRelations);
        operationLogService.batchAdd(logDTOList);
    }

    public int removeProjectMember(String projectId, String userId,String createUser, String module, String path) {
        checkProjectNotExist(projectId);
        User user = userMapper.selectByPrimaryKey(userId);
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
            LogDTO logDTO = new LogDTO(projectId, projectMapper.selectByPrimaryKey(projectId).getOrganizationId(),userRoleRelation.getId(), createUser, OperationLogType.DELETE.name(), module,Translator.get("delete") + Translator.get("project_member") + ": " + user.getName());
            setLog(logDTO,path,HttpMethodConstants.GET.name(), logDTOList);
        });
        operationLogService.batchAdd(logDTOList);
        return userRoleRelationMapper.deleteByExample(userRoleRelationExample);
    }

    public int revoke(String id) {
        checkProjectNotExist(id);
        Project project = new Project();
        project.setId(id);
        project.setDeleted(false);
        project.setDeleteTime(null);
        project.setDeleteUser(null);
        return projectMapper.updateByPrimaryKeySelective(project);
    }

    /**
     * 删除项目   一般是定时任务会触发
     * @param projects
     */
    public void deleteProject(List<Project> projects) {
        // 删除项目
        List<LogDTO> logDTOList = new ArrayList<>();
        projects.forEach(project -> {
            serviceInvoker.invokeServices(project.getId());
            LogUtils.info("send delete_project message, project id: " + project.getId());

            deleteProjectUserGroup(project.getId());
            // delete project
            projectMapper.deleteByPrimaryKey(project.getId());
            LogDTO logDTO = new LogDTO(OperationLogConstants.SYSTEM, project.getOrganizationId(),project.getId(), StringUtils.EMPTY, OperationLogType.DELETE.name(), OperationLogModule.SYSTEM_PROJECT,Translator.get("delete") + Translator.get("project") + ": " + project.getName());
            setLog(logDTO, StringUtils.EMPTY,StringUtils.EMPTY,  logDTOList);
        });
        operationLogService.batchAdd(logDTOList);
    }

    /**
     * 删除自定义用户组和权限关系表、项目和用户关系数据
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
     *
     * @param path 请求路径
     * @param method 请求方法
     * @param logDTOList 日志集合
     */
    private void setLog(LogDTO dto, String path, String method, List<LogDTO> logDTOList) {
        dto.setPath(path);
        dto.setMethod(method);
        dto.setOriginalValue(JSON.toJSONBytes(StringUtils.EMPTY));
        logDTOList.add(dto);
    }
}
