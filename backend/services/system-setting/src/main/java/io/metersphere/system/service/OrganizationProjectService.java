package io.metersphere.system.service;

import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.UserExtendDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.UpdateProjectNameRequest;
import io.metersphere.system.dto.UpdateProjectRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.OrganizationProjectRequest;
import io.metersphere.system.request.ProjectAddMemberBatchRequest;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationProjectService {

    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;
    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private OrganizationMapper organizationMapper;


    private final static String PREFIX = "/organization-project";
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
        return commonProjectService.add(addProjectDTO, createUser, ADD_PROJECT, OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }

    public List<ProjectDTO> getProjectList(OrganizationProjectRequest request) {
        ProjectRequest projectRequest = new ProjectRequest();
        BeanUtils.copyBean(projectRequest, request);
        List<ProjectDTO> projectList = extSystemProjectMapper.getProjectList(projectRequest);
        return commonProjectService.buildUserInfo(projectList);
    }

    public ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser) {
        return commonProjectService.update(updateProjectDto, updateUser, UPDATE_PROJECT, OperationLogModule.SETTING_ORGANIZATION_PROJECT);
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
                OperationLogType.ADD.name(), Translator.get("add"), OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }

    public int removeProjectMember(String projectId, String userId, String createUser) {
        return commonProjectService.removeProjectMember(projectId, userId, createUser, OperationLogModule.SETTING_ORGANIZATION_PROJECT, StringUtils.join(REMOVE_PROJECT_MEMBER, projectId, "/", userId));
    }

    public int revoke(String id, String updateUser) {
        return commonProjectService.revoke(id, updateUser);
    }

    public void enable(String id, String updateUser) {
        commonProjectService.enable(id, updateUser);
    }

    public void disable(String id, String updateUser) {
        commonProjectService.disable(id, updateUser);
    }

    public List<UserExtendDTO> getUserAdminList(String organizationId, String keyword) {
        checkOrgIsExist(organizationId);
        return extSystemProjectMapper.getUserAdminList(organizationId, keyword);
    }

    public List<UserExtendDTO> getUserMemberList(String organizationId, String projectId, String keyword) {
        checkOrgIsExist(organizationId);
        commonProjectService.checkProjectNotExist(projectId);
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userIds)) {
            return extSystemProjectMapper.getUserMemberList(userIds, projectId, keyword);
        } else {
            return null;
        }
    }

    public void checkOrgIsExist(String organizationId) {
        if (organizationMapper.selectByPrimaryKey(organizationId) == null) {
            throw new MSException(Translator.get("organization_not_exists"));
        }
    }

    public List<OptionDTO> getTestResourcePoolOptions(String organizationId) {
        return commonProjectService.getTestResourcePoolOptions(organizationId);
    }

    public void rename(UpdateProjectNameRequest project, String userId) {
        commonProjectService.rename(project, userId);
    }
}
