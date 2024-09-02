package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.*;
import io.metersphere.system.dto.request.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemProjectService {

    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;
    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private OperationLogService operationLogService;

    private final static String PREFIX = "/system/project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";
    private final static String ADD_MEMBER = PREFIX + "/add-member";

    public ProjectDTO get(String id) {
        return commonProjectService.get(id);
    }

    /**
     * @param addProjectDTO 添加项目的时候  默认给用户组添加管理员的权限
     * @return
     */
    public ProjectDTO add(AddProjectRequest addProjectDTO, String createUser) {
        return commonProjectService.add(addProjectDTO, createUser, ADD_PROJECT, OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    public List<ProjectDTO> getProjectList(ProjectRequest request) {
        List<ProjectDTO> projectList = extSystemProjectMapper.getProjectList(request);
        return commonProjectService.buildUserInfo(projectList);
    }

    public ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser) {
        return commonProjectService.update(updateProjectDto, updateUser, UPDATE_PROJECT, OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    public int delete(String id, String deleteUser) {
        return commonProjectService.delete(id, deleteUser);
    }

    public List<UserExtendDTO> getProjectMember(ProjectMemberRequest request) {
        return extSystemProjectMapper.getProjectMemberList(request);
    }

    /***
     * 添加项目成员
     * @param request
     * @param createUser
     */
    public void addProjectMember(ProjectAddMemberBatchRequest request, String createUser) {
        commonProjectService.addProjectMember(request, createUser, ADD_MEMBER,
                OperationLogType.ADD.name(), Translator.get("add"), OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    public int removeProjectMember(String projectId, String userId, String createUser) {
        return commonProjectService.removeProjectMember(projectId, userId, createUser, OperationLogModule.SETTING_SYSTEM_ORGANIZATION, StringUtils.join(REMOVE_PROJECT_MEMBER, projectId, "/", userId));
    }

    public int revoke(String id, String updateUser) {
        return commonProjectService.revoke(id, updateUser);
    }

    public void deleteProject(List<Project> projects) {
        commonProjectService.deleteProject(projects);
    }

    public List<OrganizationProjectOptionsDTO> getProjectOptions(String organizationId) {
        return extSystemProjectMapper.selectProjectOptions(organizationId);
    }

    public void enable(String id, String updateUser) {
        commonProjectService.enable(id, updateUser);
    }

    public void disable(String id, String updateUser) {
        commonProjectService.disable(id, updateUser);
    }

    public List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request) {
        return commonProjectService.getTestResourcePoolOptions(request);

    }

    public void rename(UpdateProjectNameRequest project, String userId) {
        commonProjectService.rename(project, userId);
    }

    public List<OptionDTO> list(String keyword) {
        return extSystemProjectMapper.getSystemProject(keyword);

    }

    public void addMemberByProject(ProjectAddMemberRequest request, String createUser) {
        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        Map<String, String> userMap = commonProjectService.addUserPre(request.getUserIds(), createUser, ADD_MEMBER, OperationLogModule.SETTING_SYSTEM_ORGANIZATION, request.getProjectId(), project);
        request.getUserIds().forEach(userId -> {
            request.getUserRoleIds().forEach(userRoleId -> {
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setId(IDGenerator.nextStr());
                userRoleRelation.setUserId(userId);
                userRoleRelation.setSourceId(request.getProjectId());
                userRoleRelation.setRoleId(userRoleId);
                userRoleRelation.setCreateTime(System.currentTimeMillis());
                userRoleRelation.setCreateUser(createUser);
                userRoleRelation.setOrganizationId(project.getOrganizationId());
                userRoleRelations.add(userRoleRelation);
                LogDTO logDTO = new LogDTO(OperationLogConstants.SYSTEM, OperationLogConstants.SYSTEM, userRoleRelation.getId(), createUser, OperationLogType.ADD.name(), OperationLogModule.SETTING_SYSTEM_ORGANIZATION, Translator.get("add") + Translator.get("project_member") + ": " + userMap.get(userId));
                commonProjectService.setLog(logDTO, ADD_MEMBER, HttpMethodConstants.POST.name(), logDTOList);
            });
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.batchInsert(userRoleRelations);
        }
        operationLogService.batchAdd(logDTOList);
    }
}
