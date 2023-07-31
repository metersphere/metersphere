package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.InternalUserRole;
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
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.ProjectAddMemberBatchRequest;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemProjectService {

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

    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    private final static String PREFIX = "/system/project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";

    @Autowired
    public SystemProjectService(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public Project get(String id) {
        return projectMapper.selectByPrimaryKey(id);
    }

    /**
     * @param addProjectDTO 添加项目的时候  默认给用户组添加管理员和成员的权限
     * @return
     */
    public Project add(AddProjectRequest addProjectDTO, String createUser) {

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
        projectMapper.insertSelective(project);
        ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
        memberRequest.setProjectIds(List.of(project.getId()));
        if (CollectionUtils.isEmpty(addProjectDTO.getUserIds())) {
            memberRequest.setUserIds(List.of(createUser));
        } else {
            memberRequest.setUserIds(addProjectDTO.getUserIds());
        }
        //添加项目管理员   创建的时候如果没有传管理员id  则默认创建者为管理员
        this.addProjectAdmin(memberRequest, createUser, ADD_PROJECT, OperationLogType.ADD.name(), HttpMethodConstants.POST.name(), Translator.get("add"));
        return project;
    }

    public void checkOrgRoleExit(String userId, String orgId, String createUser, String userName) {
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
        }
        setLog(orgId, "null", Translator.get("add") + Translator.get("organization_member") + ": " + userName, createUser, "", OperationLogType.ADD.name(), HttpMethodConstants.POST.name(), logDTOList);
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
    private void checkProjectNotExist(String id) {
        if (projectMapper.selectByPrimaryKey(id) == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }

    public List<ProjectDTO> getProjectList(ProjectRequest request) {
        List<ProjectDTO> projectList = extSystemProjectMapper.getProjectList(request);
        return buildUserInfo(projectList);
    }

    public List<ProjectDTO> buildUserInfo(List<ProjectDTO> projectList) {
        projectList.forEach(projectDTO -> {
            List<User> users = extSystemProjectMapper.getProjectAdminList(projectDTO.getId());
            projectDTO.setAdminList(users);
        });
        return projectList;
    }

    public Project update(UpdateProjectRequest updateProjectDto, String updateUser) {
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
                userRoleRelationMapper.deleteByExample(deleteExample);
            }
            if (CollectionUtils.isNotEmpty(insertIds)) {
                ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
                memberRequest.setProjectIds(List.of(project.getId()));
                memberRequest.setUserIds(insertIds);
                this.addProjectAdmin(memberRequest, updateUser, UPDATE_PROJECT, OperationLogType.UPDATE.name(),
                        HttpMethodConstants.POST.name(), Translator.get("update"));
            }
        } else {
            if (CollectionUtils.isNotEmpty(orgUserIds)) {
                //如果前端传过来的用户id为空  则删除项目所有管理员
                UserRoleRelationExample deleteExample = new UserRoleRelationExample();
                deleteExample.createCriteria().andSourceIdEqualTo(project.getId()).andUserIdIn(orgUserIds).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
                userRoleRelationMapper.deleteByExample(deleteExample);
            }
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

    public List<UserExtend> getProjectMember(ProjectMemberRequest request) {
        List<UserExtend> projectMemberList = extSystemProjectMapper.getProjectMemberList(request);
        return projectMemberList;
    }

    /**
     * 添加项目管理员
     *
     * @param request
     * @param createUser
     * @param path
     * @param type
     * @param method
     * @param content
     */
    public void addProjectAdmin(ProjectAddMemberBatchRequest request, String createUser, String path, String type,
                                String method, String content) {

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
                this.checkOrgRoleExit(userId, projectMapper.selectByPrimaryKey(projectId).getOrganizationId(), createUser, user.getName());
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
                    setLog(projectId, path, content + Translator.get("project_admin") + ": " + user.getName(), createUser, "", type, method, logDTOList);
                }
            });
        });
        userRoleRelationMapper.batchInsert(userRoleRelations);
        operationLogService.batchAdd(logDTOList);
    }

    /***
     * 添加项目成员
     * @param request
     * @param createUser
     * @param path 请求路径
     * @param type 操作类型
     * @param method 请求方法
     * @param content 操作内容
     */
    public void addProjectMember(ProjectAddMemberBatchRequest request, String createUser, String path, String type,
                                 String method, String content) {

        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            checkProjectNotExist(projectId);
            request.getUserIds().forEach(userId -> {
                User user = userMapper.selectByPrimaryKey(userId);
                if (ObjectUtils.isEmpty(user)) {
                    throw new MSException(Translator.get("user_not_exist"));
                }
                this.checkOrgRoleExit(userId, projectMapper.selectByPrimaryKey(projectId).getOrganizationId(), createUser, user.getName());
                UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
                userRoleRelationExample.createCriteria().andUserIdEqualTo(userId)
                        .andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_MEMBER.getValue());
                if (userRoleRelationMapper.selectByExample(userRoleRelationExample).size() == 0) {
                    UserRoleRelation memberRole = new UserRoleRelation();
                    memberRole.setId(UUID.randomUUID().toString());
                    memberRole.setUserId(userId);
                    memberRole.setRoleId(InternalUserRole.PROJECT_MEMBER.getValue());
                    memberRole.setSourceId(projectId);
                    memberRole.setCreateTime(System.currentTimeMillis());
                    memberRole.setCreateUser(createUser);
                    userRoleRelations.add(memberRole);
                    setLog(projectId, path, content + Translator.get("project_member") + ": " + user.getName(), createUser, "", type, method, logDTOList);
                }
            });
        });
        userRoleRelationMapper.batchInsert(userRoleRelations);
        operationLogService.batchAdd(logDTOList);
    }

    public int removeProjectMember(String projectId, String userId) {
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId)
                .andSourceIdEqualTo(projectId);
        User user = userMapper.selectByPrimaryKey(userId);
        if (StringUtils.equals(projectId, user.getLastProjectId())) {
            user.setLastProjectId(StringUtils.EMPTY);
            userMapper.updateByPrimaryKeySelective(user);
        }
        List<LogDTO> logDTOList = new ArrayList<>();
        setLog(projectId, REMOVE_PROJECT_MEMBER + "/" + projectId + "/" + userId,
                Translator.get("delete") + Translator.get("project_member") + ": " + user.getName(),
                userId, "", OperationLogType.DELETE.name(), HttpMethodConstants.GET.name(), logDTOList);
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

    public void deleteProject(List<Project> projects) {
        // 删除项目
        List<LogDTO> logDTOList = new ArrayList<>();
        projects.forEach(project -> {
            serviceInvoker.invokeServices(project.getId());
            LogUtils.info("send delete_project message, project id: " + project.getId());
            //删除项目关联的自定义组织
            deleteProjectUserGroup(project.getId());
            // delete project
            projectMapper.deleteByPrimaryKey(project.getId());
            setLog(project.getId(), "null", Translator.get("delete") + Translator.get("project") + ": " + project.getName(),
                    "system", "", OperationLogType.DELETE.name(), "", logDTOList);
        });
        operationLogService.batchAdd(logDTOList);
    }

    private void deleteProjectUserGroup(String projectId) {
        UserRoleRelationExample userGroupExample = new UserRoleRelationExample();
        userGroupExample.createCriteria().andSourceIdEqualTo(projectId);
        userRoleRelationMapper.deleteByExample(userGroupExample);
    }

    public List<OrganizationProjectOptionsDTO> getProjectOptions() {
        return extSystemProjectMapper.selectProjectOptions();
    }

    private static void setLog(String projectId, String path, String content, String userId, Object originalValue,
                               String type, String method, List<LogDTO> logDTOList) {
        LogDTO dto = new LogDTO(
                "system",
                "system",
                projectId,
                userId,
                type,
                OperationLogModule.SYSTEM_PROJECT,
                content);
        dto.setPath(path);
        dto.setMethod(method);
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        logDTOList.add(dto);
    }
}
