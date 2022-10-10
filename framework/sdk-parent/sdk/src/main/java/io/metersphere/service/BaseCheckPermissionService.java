package io.metersphere.service;

import io.metersphere.base.domain.Group;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.UserGroup;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.BaseProjectMapper;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseCheckPermissionService {
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    private ProjectMapper projectMapper;


    public Set<String> getUserRelatedProjectIds() {
        UserDTO userDTO = baseUserService.getUserDTO(SessionUtils.getUserId());
        List<String> groupIds = userDTO.getGroups()
                .stream()
                .filter(g -> StringUtils.equals(g.getType(), UserGroupType.PROJECT))
                .map(Group::getId)
                .collect(Collectors.toList());
        return userDTO.getUserGroups().stream()
                .filter(ur -> groupIds.contains(ur.getGroupId()))
                .map(UserGroup::getSourceId)
                .collect(Collectors.toSet());
    }


    public List<ProjectDTO> getOwnerProjects() {
        Set<String> userRelatedProjectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(userRelatedProjectIds)) {
            return new ArrayList<>(0);
        }
        List<String> projectIds = new ArrayList<>(userRelatedProjectIds);
        return baseProjectMapper.queryListByIds(projectIds);
    }


    public void checkProjectBelongToWorkspace(String projectId, String workspaceId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null || !StringUtils.equals(project.getWorkspaceId(), workspaceId)) {
            MSException.throwException(Translator.get("check_owner_project"));
        }
    }

    public Set<String> getOwnerByUserId(String userId) {
        UserDTO userDTO = baseUserService.getUserDTO(userId);
        List<String> groupIds = userDTO.getGroups()
                .stream()
                .filter(g -> StringUtils.equals(g.getType(), UserGroupType.PROJECT))
                .map(Group::getId)
                .collect(Collectors.toList());
        return userDTO.getUserGroups().stream()
                .filter(ur -> groupIds.contains(ur.getGroupId()))
                .map(UserGroup::getSourceId)
                .collect(Collectors.toSet());
    }

    public void checkProjectOwner(String projectId) {
        Set<String> projectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        if (!projectIds.contains(projectId)) {
            MSException.throwException(Translator.get("check_owner_project"));
        }
    }
}
