package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.UserExtendDTO;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.*;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.request.ProjectAddMemberBatchRequest;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemProjectService {

    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;
    @Resource
    private CommonProjectService commonProjectService;

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

    public List<OptionDTO> getTestResourcePoolOptions(String organizationId) {
        return commonProjectService.getTestResourcePoolOptions(organizationId);

    }

    public void rename(UpdateProjectNameRequest project, String userId) {
        commonProjectService.rename(project, userId);
    }
}
