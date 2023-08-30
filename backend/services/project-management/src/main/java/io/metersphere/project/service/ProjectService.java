package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.ProjectSwitchRequest;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.dto.ProjectExtendDTO;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.service.BaseUserService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
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
        SessionUtils.putUser(SessionUser.fromUser(userDTO, SessionUtils.getSessionId()));
        return SessionUtils.getUser();
    }

    public ProjectExtendDTO getProjectById(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        ProjectExtendDTO projectExtendDTO = new ProjectExtendDTO();
        if (ObjectUtils.isNotEmpty(project)) {
            BeanUtils.copyBean(projectExtendDTO, project);
            if (StringUtils.isNotEmpty(project.getModuleSetting())) {
                projectExtendDTO.setModuleIds(JSON.parseArray(project.getModuleSetting(), String.class));
            }
        } else {
            return null;
        }
        return projectExtendDTO;
    }
}
