package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.UserRoleConstants;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public Project get(String id) {
        return projectMapper.selectByPrimaryKey(id);
    }

    public Project add(Project project) {
        //TODO  添加项目需要检查配额  这个需要等后续定下来补全逻辑

        project.setName(project.getName().trim());
        checkProjectExist(project);
        project.setId(UUID.randomUUID().toString());
        project.setCreateTime(System.currentTimeMillis());
        project.setUpdateTime(System.currentTimeMillis());
        projectMapper.insertSelective(project);
        return project;
    }

    private void checkProjectExist(Project project) {
        if (project.getName() != null) {
            ProjectExample example = new ProjectExample();
            example.createCriteria().andNameEqualTo(project.getName()).andOrganizationIdEqualTo(project.getOrganizationId()).andIdNotEqualTo(project.getId());
            if (projectMapper.selectByExample(example).size() > 0) {
                throw new MSException(Translator.get("project_name_already_exists"));
            }
        }
    }

    public List<ProjectDTO> getProjectList(ProjectRequest request) {
        List<ProjectDTO> projectList = extSystemProjectMapper.getProjectList(request);
        return projectList;
    }

    public void update(Project project) {
        project.setCreateUser(null);
        project.setCreateTime(null);
        project.setUpdateTime(System.currentTimeMillis());
        checkProjectExist(project);
        projectMapper.updateByPrimaryKeySelective(project);
    }

    public void delete(Project project) {
        //TODO  删除项目删除全部资源 这里的删除只是假删除
        project.setDeleted(true);
        project.setCreateUser(null);
        project.setCreateTime(null);
        project.setDeleteTime(System.currentTimeMillis());
        projectMapper.updateByPrimaryKeySelective(project);
    }

    public List<User> getProjectMember(ProjectRequest request) {
        List<User> projectMemberList = extSystemProjectMapper.getProjectMemberList(request);
        if (CollectionUtils.isEmpty(projectMemberList)) {
            return new ArrayList<>();
        }
        return projectMemberList;
    }

    public void addProjectMember(ProjectMemberRequest request) {
        //TODO  添加项目成员需要检查配额  这个需要等后续定下来补全逻辑

        request.getUserIds().forEach(userId -> {
            UserRoleRelation userRoleRelation = new UserRoleRelation(
                    UUID.randomUUID().toString(),
                    userId,
                    UserRoleConstants.PROJECT_MEMBER,
                    request.getProjectId(),
                    System.currentTimeMillis(),
                    request.getCreateUser());
            userRoleRelationMapper.insertSelective(userRoleRelation);
        });

    }

    public void removeProjectMember(String projectId, String userId) {
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId)
                .andSourceIdEqualTo(projectId);
        User user = userMapper.selectByPrimaryKey(userId);
        if (StringUtils.equals(projectId, user.getLastProjectId())) {
            user.setLastProjectId(StringUtils.EMPTY);
            userMapper.updateByPrimaryKeySelective(user);
        }
        userRoleRelationMapper.deleteByExample(userRoleRelationExample);
    }

    public void revoke(String id) {
        Project project = new Project();
        project.setId(id);
        project.setDeleted(false);
        project.setDeleteTime(null);
        project.setCreateUser(null);
        project.setCreateTime(null);
        project.setDeleteUser(null);
        projectMapper.updateByPrimaryKeySelective(project);
    }

    public List<Project> getProjectListByOrg(String organizationId) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andOrganizationIdEqualTo(organizationId);
        return projectMapper.selectByExample(example);
    }

    public String getLogDetails(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        if (ObjectUtils.isNotEmpty(project)) {
            return project.getName();
        }
        return null;
    }
}
