package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.dto.AddProjectRequest;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.dto.UpdateProjectRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.OrganizationProjectOptionsDto;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.ProjectAddMemberRequest;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Project get(String id) {
        return projectMapper.selectByPrimaryKey(id);
    }

    /**
     *
     * @param addProjectDTO
     * 添加项目的时候  默认给用户组添加管理员和成员的权限
     * @return
     */
    public Project add(AddProjectRequest addProjectDTO , String createUser) {
        //TODO  添加项目需要检查配额  这个需要等后续定下来补全逻辑

        Project project = new Project();
        project.setId(UUID.randomUUID().toString());
        project.setName(addProjectDTO.getName());
        project.setOrganizationId(addProjectDTO.getOrganizationId());
        checkProjectExist(project);
        project.setCreateTime(System.currentTimeMillis());
        project.setUpdateTime(System.currentTimeMillis());
        project.setUpdateUser(createUser);
        project.setCreateUser(createUser);
        project.setEnable(addProjectDTO.getEnable());
        project.setDescription(addProjectDTO.getDescription());
        projectMapper.insertSelective(project);
        ProjectAddMemberRequest memberRequest = new ProjectAddMemberRequest();
        memberRequest.setProjectId(project.getId());
        memberRequest.setUserIds(addProjectDTO.getUserIds());
        this.addProjectMember(memberRequest, createUser, true);
        return project;
    }

    private void checkProjectExist(Project project) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andNameEqualTo(project.getName()).andOrganizationIdEqualTo(project.getOrganizationId());
        if (projectMapper.selectByExample(example).size() > 0) {
            throw new MSException(Translator.get("project_name_already_exists"));
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
        checkProjectExist(project);
        if (ObjectUtils.isEmpty(projectMapper.selectByPrimaryKey(project.getId()))) {
            return null;
        }
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(project.getId()).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        List<String> orgUserIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
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
            ProjectAddMemberRequest memberRequest = new ProjectAddMemberRequest();
            memberRequest.setProjectId(project.getId());
            memberRequest.setUserIds(insertIds);
            this.addProjectMember(memberRequest, updateUser, true);
        }

        projectMapper.updateByPrimaryKeySelective(project);
        return project;
    }

    public int delete(String id, String deleteUser) {
        //TODO  删除项目删除全部资源 这里的删除只是假删除
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

    public void addProjectMember(ProjectAddMemberRequest request, String createUser, boolean isAdmin) {
        //TODO  添加项目成员需要检查配额  这个需要等后续定下来补全逻辑
        request.getUserIds().forEach(userId -> {
            User user = userMapper.selectByPrimaryKey(userId);
            if (ObjectUtils.isEmpty(user)) {
                throw new MSException(Translator.get("user_not_exist"));
            }
            if (isAdmin) {
                UserRoleRelation adminRole = new UserRoleRelation(
                        UUID.randomUUID().toString(),
                        userId,
                        InternalUserRole.PROJECT_ADMIN.getValue(),
                        request.getProjectId(),
                        System.currentTimeMillis(),
                        createUser);
                userRoleRelationMapper.insertSelective(adminRole);
            }
            UserRoleRelation memberRole = new UserRoleRelation(
                    UUID.randomUUID().toString(),
                    userId,
                    InternalUserRole.PROJECT_MEMBER.getValue(),
                    request.getProjectId(),
                    System.currentTimeMillis(),
                    createUser);
            userRoleRelationMapper.insertSelective(memberRole);
        });

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
        //写入操作日志
        return userRoleRelationMapper.deleteByExample(userRoleRelationExample);
    }

    public int revoke(String id) {
        Project project = new Project();
        project.setId(id);
        project.setDeleted(false);
        project.setDeleteTime(null);
        project.setDeleteUser(null);
        return projectMapper.updateByPrimaryKeySelective(project);
    }

    public void deleteProject(List<Project> projects) {
        // 删除项目
        projects.forEach(project -> {
            LoggerUtil.info("send delete_project message, project id: " + project.getId());
            //删除项目关联的自定义组织
            deleteProjectUserGroup(project.getId());

            //TODO 需要删除环境，文件管理，各个资源涉及到的，定时任务

            // delete project
            projectMapper.deleteByPrimaryKey(project.getId());
        });
    }

    private void deleteProjectUserGroup(String projectId) {
        UserRoleRelationExample userGroupExample = new UserRoleRelationExample();
        userGroupExample.createCriteria().andSourceIdEqualTo(projectId);
        userRoleRelationMapper.deleteByExample(userGroupExample);
    }

    public List<OrganizationProjectOptionsDto> getprojectOptions() {
        return extSystemProjectMapper.selectProjectOptions();
    }
}
