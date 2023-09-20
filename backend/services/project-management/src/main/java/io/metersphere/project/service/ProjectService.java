package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.ProjectSwitchRequest;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.dto.ProjectExtendDTO;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.sdk.dto.UpdateProjectRequest;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.service.BaseUserService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.CommonProjectService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ProjectExtendDTO getProjectById(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        ProjectExtendDTO projectExtendDTO = new ProjectExtendDTO();
        if (ObjectUtils.isNotEmpty(project)) {
            BeanUtils.copyBean(projectExtendDTO, project);
            Organization organization = organizationMapper.selectByPrimaryKey(project.getOrganizationId());
            projectExtendDTO.setOrganizationName(organization.getName());
            if (StringUtils.isNotEmpty(project.getModuleSetting())) {
                projectExtendDTO.setModuleIds(JSON.parseArray(project.getModuleSetting(), String.class));
            }
        } else {
            return null;
        }
        return projectExtendDTO;
    }

    public ProjectExtendDTO update(UpdateProjectRequest updateProjectDto, String updateUser) {
        Project project = new Project();
        ProjectExtendDTO projectExtendDTO = new ProjectExtendDTO();
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
        projectExtendDTO.setOrganizationName(organizationMapper.selectByPrimaryKey(updateProjectDto.getOrganizationId()).getName());
        BeanUtils.copyBean(projectExtendDTO, project);
        //判断是否有模块设置
        if (CollectionUtils.isNotEmpty(updateProjectDto.getModuleIds())) {
            project.setModuleSetting(JSON.toJSONString(updateProjectDto.getModuleIds()));
            projectExtendDTO.setModuleIds(updateProjectDto.getModuleIds());
        }

        projectMapper.updateByPrimaryKeySelective(project);
        return projectExtendDTO;
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

    public int delete(String id, String deleteUser) {
        return commonProjectService.delete(id,deleteUser);
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
}
