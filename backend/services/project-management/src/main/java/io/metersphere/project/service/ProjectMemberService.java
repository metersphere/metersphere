package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ProjectUserDTO;
import io.metersphere.project.mapper.ExtProjectMemberMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.ProjectMemberAddRequest;
import io.metersphere.project.request.ProjectMemberBatchDeleteRequest;
import io.metersphere.project.request.ProjectMemberEditRequest;
import io.metersphere.project.request.ProjectMemberRequest;
import io.metersphere.sdk.constants.UserRoleEnum;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.BaseUserMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.mapper.ExtOrganizationMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.OrganizationRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author song-cc-rock
 */
@Service
@Transactional
public class ProjectMemberService {

    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;
    @Resource
    private ExtProjectMemberMapper extProjectMemberMapper;

    /**
     * 获取成员列表
     *
     * @param request 请求参数
     * @return 成员列表
     */
    public List<ProjectUserDTO> listMember(ProjectMemberRequest request) {
        // 查询当前项目成员
        List<String> members = extProjectMemberMapper.listMember(request);
        UserRoleRelationExample relationExample = new UserRoleRelationExample();
        relationExample.createCriteria().andSourceIdEqualTo(request.getProjectId()).andUserIdIn(members);
        List<UserRoleRelation> userRoleRelates = userRoleRelationMapper.selectByExample(relationExample);
        Map<String, List<String>> userRoleRelateMap = userRoleRelates.stream().collect(Collectors.groupingBy(UserRoleRelation::getUserId,
                Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        // 查询所有用户
        List<User> users = baseUserMapper.findAll();
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        // 查询所有项目类型用户组
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andTypeEqualTo(UserRoleType.PROJECT.name());
        List<UserRole> roles = userRoleMapper.selectByExample(example);
        Map<String, UserRole> roleMap = roles.stream().collect(Collectors.toMap(UserRole::getId, role -> role));
        List<ProjectUserDTO> projectUsers = new ArrayList<>();
        userRoleRelateMap.forEach((k, v) -> {
            ProjectUserDTO projectUser = new ProjectUserDTO();
            User user = userMap.get(k);
            BeanUtils.copyBean(projectUser, user);
            List<UserRole> userRoles = new ArrayList<>();
            v.forEach(roleId -> {
                UserRole role = roleMap.get(roleId);
                userRoles.add(role);
            });
            projectUser.setUserRoles(userRoles);
            projectUsers.add(projectUser);
        });
        return projectUsers;
    }

    /**
     * 获取组织成员下拉选项
     *
     * @param projectId 项目ID
     * @return 项目成员下拉选项
     */
    public List<UserExtend> getMemberOption(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return new ArrayList<>();
        }
        return extOrganizationMapper.getMemberByOrg(project.getOrganizationId());
    }

    /**
     * 获取项目用户组下拉选项
     *
     * @param projectId 项目ID
     * @return 用户组下拉选项
     */
    public List<OptionDTO> getRoleOption(String projectId) {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andTypeEqualTo(UserRoleType.PROJECT.name())
                .andScopeIdIn(Arrays.asList(projectId, UserRoleEnum.GLOBAL.toString()));
        List<UserRole> userRoles = userRoleMapper.selectByExample(example);
        return userRoles.stream().map(userRole -> new OptionDTO(userRole.getId(), userRole.getName())).toList();
    }

    /**
     * 添加成员(项目)
     *
     * @param request 请求参数
     * @param currentUserId 当前用户ID
     */
    public void addMember(ProjectMemberAddRequest request, String currentUserId) {
        // 项目不存在, 则不添加
        checkProjectExist(request.getProjectId());
        // 获取已经存在的用户组
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(request.getProjectId())
                .andUserIdIn(request.getUserIds()).andRoleIdIn(request.getRoleIds());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        Map<String, List<String>> existUserRelations = userRoleRelations.stream().collect(
                Collectors.groupingBy(UserRoleRelation::getUserId, Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        // 比较用户组是否已经存在, 如果不存在则添加
        List<UserRoleRelation> relations = new ArrayList<>();
        request.getUserIds().forEach(userId -> request.getRoleIds().forEach(roleId -> {
            // 用户不存在或用户组不存在, 则不添加
            if (isUserOrRoleNotExist(userId, roleId)) {
                return;
            }
            // 如果该用户已经添加至该用户组, 则不再添加
            if (existUserRelations.containsKey(userId) && existUserRelations.get(userId).contains(roleId)) {
                return;
            }
            UserRoleRelation relation = new UserRoleRelation();
            relation.setId(UUID.randomUUID().toString());
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setSourceId(request.getProjectId());
            relation.setCreateTime(System.currentTimeMillis());
            relation.setCreateUser(currentUserId);
            relations.add(relation);
        }));
        if (!CollectionUtils.isEmpty(relations)) {
            userRoleRelationMapper.batchInsert(relations);
        }
    }

    /**
     * 更新成员(项目)
     *
     * @param request 请求参数
     * @param currentUserId 当前用户ID
     */
    public void updateMember(ProjectMemberEditRequest request, String currentUserId) {
        // 项目不存在
        checkProjectExist(request.getProjectId());
        // 移除已经存在的用户组
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(request.getProjectId())
                .andUserIdEqualTo(request.getUserId());
        userRoleRelationMapper.deleteByExample(example);
        // 添加新的用户组
        List<UserRoleRelation> relations = new ArrayList<>();
        request.getRoleIds().forEach(roleId -> {
            // 用户不存在或用户组不存在, 则不添加
            if (isUserOrRoleNotExist(request.getUserId(), roleId)) {
                return;
            }
            UserRoleRelation relation = new UserRoleRelation();
            relation.setId(UUID.randomUUID().toString());
            relation.setUserId(request.getUserId());
            relation.setRoleId(roleId);
            relation.setSourceId(request.getProjectId());
            relation.setCreateTime(System.currentTimeMillis());
            relation.setCreateUser(currentUserId);
            relations.add(relation);
        });
        if (!CollectionUtils.isEmpty(relations)) {
            userRoleRelationMapper.batchInsert(relations);
        }
    }

    /**
     * 移除成员(项目)
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     */
    public void removeMember(String projectId, String userId) {
        // 项目不存在, 则不移除
        checkProjectExist(projectId);
        // 移除成员, 则移除该成员在该项目下的所有用户组
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(projectId).andUserIdEqualTo(userId);
        userRoleRelationMapper.deleteByExample(example);
    }

    /**
     * 批量添加成员至用户组(项目)
     * @param request 请求参数
     * @param currentUserId 当前用户ID
     */
    public void addMemberRole(ProjectMemberAddRequest request, String currentUserId) {
        // 添加用户用户组(已经添加的用户组不再添加)
        addMember(request, currentUserId);
    }

    /**
     * 批量移除成员(项目)
     * @param request 请求参数
     */
    public void batchRemove(ProjectMemberBatchDeleteRequest request) {
        // 项目不存在, 则不移除
        checkProjectExist(request.getProjectId());
        // 批量移除成员, 则移除该成员在该项目下的所有用户组
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(request.getProjectId())
                .andUserIdIn(request.getUserIds());
        userRoleRelationMapper.deleteByExample(example);
    }

    private void checkProjectExist(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            throw new MSException(Translator.get("project_not_exist"));
        }
    }

    private boolean isUserOrRoleNotExist(String userId, String roleId) {
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andDeletedEqualTo(false);
        return userMapper.selectByExample(example) == null || userRoleMapper.selectByPrimaryKey(roleId) == null;
    }
}
