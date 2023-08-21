package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.sdk.dto.AddProjectRequest;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.dto.UpdateProjectRequest;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import io.metersphere.system.dto.UserExtend;
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

    public Project get(String id) {
        return commonProjectService.get(id);
    }

    /**
     * @param addProjectDTO 添加项目的时候  默认给用户组添加管理员的权限
     * @return
     */
    public Project add(AddProjectRequest addProjectDTO, String createUser) {
        return commonProjectService.add(addProjectDTO, createUser, ADD_PROJECT, OperationLogModule.SYSTEM_PROJECT);
    }

    public List<ProjectDTO> getProjectList(ProjectRequest request) {
        List<ProjectDTO> projectList = extSystemProjectMapper.getProjectList(request);
        return commonProjectService.buildUserInfo(projectList);
    }

    public Project update(UpdateProjectRequest updateProjectDto, String updateUser) {
        return commonProjectService.update(updateProjectDto, updateUser, UPDATE_PROJECT, OperationLogModule.SYSTEM_PROJECT);
    }

    public int delete(String id, String deleteUser) {
        return commonProjectService.delete(id, deleteUser);
    }

    public List<UserExtend> getProjectMember(ProjectMemberRequest request) {
        commonProjectService.checkProjectNotExist(request.getProjectId());
        return extSystemProjectMapper.getProjectMemberList(request);
    }

    /***
     * 添加项目成员
     * @param request
     * @param createUser
     */
    public void addProjectMember(ProjectAddMemberBatchRequest request, String createUser) {
        commonProjectService.addProjectMember(request, createUser, ADD_MEMBER,
                OperationLogType.ADD.name(), Translator.get("add"), OperationLogModule.SYSTEM_PROJECT);
    }

    public int removeProjectMember(String projectId, String userId, String createUser) {
        return commonProjectService.removeProjectMember(projectId, userId, createUser,OperationLogModule.SYSTEM_PROJECT, StringUtils.join(REMOVE_PROJECT_MEMBER,projectId, "/", userId));
    }

    public int revoke(String id) {
        return commonProjectService.revoke(id);
    }

    public void deleteProject(List<Project> projects) {
        commonProjectService.deleteProject(projects);
    }

    public List<OrganizationProjectOptionsDTO> getProjectOptions(String organizationId) {
        return extSystemProjectMapper.selectProjectOptions(organizationId);
    }

    public void enable(String id) {
        commonProjectService.enable(id);
    }

    public void disable(String id) {
        commonProjectService.disable(id);
    }
}
