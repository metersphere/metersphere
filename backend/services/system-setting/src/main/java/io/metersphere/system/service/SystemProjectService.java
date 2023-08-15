package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.dto.AddProjectRequest;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.dto.UpdateProjectRequest;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import io.metersphere.system.dto.ProjectExtendDTO;
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
    private ProjectMapper projectMapper;
    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;
    @Resource
    private CommonProjectService commonProjectService;

    private final static String PREFIX = "/system/project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";
    private final static String ADD_MEMBER = PREFIX + "/add-member";

    public ProjectExtendDTO get(String id) {
        return commonProjectService.get(id);
    }

    /**
     * @param addProjectDTO 添加项目的时候  默认给用户组添加管理员的权限
     * @return
     */
    public ProjectExtendDTO add(AddProjectRequest addProjectDTO, String createUser) {
        ProjectExtendDTO project = commonProjectService.add(addProjectDTO, createUser, ADD_PROJECT, OperationLogModule.SYSTEM_PROJECT);
        return project;
    }

    public List<ProjectDTO> getProjectList(ProjectRequest request) {
        List<ProjectDTO> projectList = extSystemProjectMapper.getProjectList(request);
        return commonProjectService.buildUserInfo(projectList);
    }

    public ProjectExtendDTO update(UpdateProjectRequest updateProjectDto, String updateUser) {
        ProjectExtendDTO project = commonProjectService.update(updateProjectDto, updateUser, UPDATE_PROJECT, OperationLogModule.SYSTEM_PROJECT);
        return project;
    }

    public int delete(String id, String deleteUser) {
        return commonProjectService.delete(id, deleteUser);
    }

    public List<UserExtend> getProjectMember(ProjectMemberRequest request) {
        commonProjectService.checkProjectNotExist(request.getProjectId());
        List<UserExtend> projectMemberList = extSystemProjectMapper.getProjectMemberList(request);
        return projectMemberList;
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


    public List<OrganizationProjectOptionsDTO> getProjectOptions() {
        return extSystemProjectMapper.selectProjectOptions();
    }
}
