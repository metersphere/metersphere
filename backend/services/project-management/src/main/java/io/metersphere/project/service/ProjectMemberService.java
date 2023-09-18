package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ProjectUserDTO;
import io.metersphere.project.mapper.ExtProjectMemberMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.ProjectMemberAddRequest;
import io.metersphere.project.request.ProjectMemberBatchDeleteRequest;
import io.metersphere.project.request.ProjectMemberEditRequest;
import io.metersphere.project.request.ProjectMemberRequest;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.UserRoleEnum;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.UserExtend;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.uid.UUID;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private ProjectMapper projectMapper;
    @Resource
    private ExtProjectMemberMapper extProjectMemberMapper;
    @Resource
    private OperationLogService operationLogService;

    /**
     * 获取成员列表
     *
     * @param request 请求参数
     * @return 成员列表
     */
    public List<ProjectUserDTO> listMember(ProjectMemberRequest request) {
        // 查询当前项目成员
        List<String> members = extProjectMemberMapper.listMember(request);
        if (CollectionUtils.isEmpty(members)) {
            return new ArrayList<>();
        }
        UserRoleRelationExample relationExample = new UserRoleRelationExample();
        relationExample.createCriteria().andSourceIdEqualTo(request.getProjectId()).andUserIdIn(members);
        List<UserRoleRelation> userRoleRelates = userRoleRelationMapper.selectByExample(relationExample);
        Map<String, List<String>> userRoleRelateMap = userRoleRelates.stream().collect(Collectors.groupingBy(UserRoleRelation::getUserId,
                Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        // 查询所有项目类型用户组
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andTypeEqualTo(UserRoleType.PROJECT.name());
        List<UserRole> roles = userRoleMapper.selectByExample(example);
        Map<String, UserRole> roleMap = roles.stream().collect(Collectors.toMap(UserRole::getId, role -> role));
        List<ProjectUserDTO> projectUsers = new ArrayList<>();
        userRoleRelateMap.forEach((k, v) -> {
            ProjectUserDTO projectUser = new ProjectUserDTO();
            projectUser.setId(k);
            List<UserRole> userRoles = new ArrayList<>();
            v.forEach(roleId -> {
                UserRole role = roleMap.get(roleId);
                userRoles.add(role);
            });
            projectUser.setUserRoles(userRoles);
            projectUsers.add(projectUser);
        });
        // 设置用户信息
        List<String> uerIds = projectUsers.stream().map(ProjectUserDTO::getId).collect(Collectors.toList());
        UserExample userExample = new UserExample();
        userExample.createCriteria().andDeletedEqualTo(false).andIdIn(uerIds);
        List<User> users = userMapper.selectByExample(userExample);
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        projectUsers.forEach(projectUser -> {
            User user = userMap.get(projectUser.getId());
            BeanUtils.copyBean(projectUser, user);
        });
        projectUsers.sort(Comparator.comparing(ProjectUserDTO::getName));
        return projectUsers;
    }

    /**
     * 获取组织成员下拉选项
     *
     * @param projectId 项目ID
     * @return 项目成员下拉选项
     */
    public List<UserExtend> getMemberOption(String projectId, String keyword) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return new ArrayList<>();
        }
        // 组织成员
        List<UserExtend> orgMembers = extProjectMemberMapper.getMemberByOrg(project.getOrganizationId(), keyword);
        if (CollectionUtils.isEmpty(orgMembers)) {
            return new ArrayList<>();
        }
        // 设置是否是项目成员
        List<String> orgMemberIds = orgMembers.stream().map(UserExtend::getId).toList();
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdIn(orgMemberIds).andSourceIdEqualTo(projectId).andOrganizationIdEqualTo(project.getOrganizationId());
        List<UserRoleRelation> projectRelations = userRoleRelationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(projectRelations)) {
            orgMembers.forEach(orgMember -> orgMember.setMemberFlag(false));
        } else {
            List<String> projectUsers = projectRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();
            // 已经是项目成员的组织成员, 禁用
            orgMembers.forEach(orgMember -> orgMember.setMemberFlag(projectUsers.contains(orgMember.getId())));
        }
        return orgMembers;
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
     * @param request       请求参数
     * @param currentUserId 当前用户ID
     */
    public void addMember(ProjectMemberAddRequest request, String currentUserId) {
        addMemberRole(request, currentUserId, OperationLogType.ADD.name(), "/project/member/add");
    }

    /**
     * 更新成员(项目)
     *
     * @param request       请求参数
     * @param currentUserId 当前用户ID
     */
    public void updateMember(ProjectMemberEditRequest request, String currentUserId) {
        // 操作记录
        List<LogDTO> logs = new ArrayList<>();
        // 项目不存在
        Project project = checkProjectExist(request.getProjectId());
        // 移除已经存在的用户组
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(request.getProjectId())
                .andUserIdEqualTo(request.getUserId());
        // 旧的用户组关系, 操作记录使用
        List<UserRoleRelation> oldRelations = userRoleRelationMapper.selectByExample(example);
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
            relation.setOrganizationId(project.getOrganizationId());
            relations.add(relation);
        });
        if (!CollectionUtils.isEmpty(relations)) {
            userRoleRelationMapper.batchInsert(relations);
        }
        // 操作记录
        UserRoleExample roleExample = new UserRoleExample();
        roleExample.createCriteria().andIdIn(request.getRoleIds());
        List<UserRole> newRoles = userRoleMapper.selectByExample(roleExample);
        List<UserRole> oldRoles = new ArrayList<>();
        if (!CollectionUtils.isEmpty(oldRelations)) {
            List<String> oldRoleIds = oldRelations.stream().map(UserRoleRelation::getRoleId).toList();
            roleExample.clear();
            roleExample.createCriteria().andIdIn(oldRoleIds);
            oldRoles = userRoleMapper.selectByExample(roleExample);
        }
        setLog(request.getProjectId(), request.getUserId(), currentUserId, OperationLogType.UPDATE.name(), "/project/member/update", HttpMethodConstants.POST.name(), oldRoles, newRoles, logs);
        operationLogService.batchAdd(logs);
    }

    /**
     * 移除成员(项目)
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     */
    public void removeMember(String projectId, String userId, String currentUserId) {
        // 操作记录
        List<LogDTO> logs = new ArrayList<>();
        // 项目不存在, 则不移除
        checkProjectExist(projectId);
        // 移除成员, 则移除该成员在该项目下的所有用户组
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(projectId).andUserIdEqualTo(userId);
        userRoleRelationMapper.deleteByExample(example);
        // 操作记录
        setLog(projectId, userId, currentUserId, OperationLogType.DELETE.name(), "/project/member/remove", HttpMethodConstants.GET.name(), null, null, logs);
        operationLogService.batchAdd(logs);
    }

    /**
     * 批量添加成员至用户组(项目)
     *
     * @param request       请求参数
     * @param currentUserId 当前用户ID
     */
    public void addRole(ProjectMemberAddRequest request, String currentUserId) {
        // 添加用户用户组(已经添加的用户组不再添加)
        addMemberRole(request, currentUserId, OperationLogType.UPDATE.name(), "/project/member/add-role");
    }

    /**
     * 处理成员及用户组关系, 并生成操作记录
     *
     * @param request       请求参数
     * @param currentUserId 创建人
     * @param operationType 操作记录类型
     * @param path          操作记录路径
     */
    public void addMemberRole(ProjectMemberAddRequest request, String currentUserId, String operationType, String path) {
        // 操作记录
        List<LogDTO> logs = new ArrayList<>();
        // 项目不存在, 则不添加
        Project project = checkProjectExist(request.getProjectId());
        // 获取已经存在的用户组
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(request.getProjectId())
                .andUserIdIn(request.getUserIds()).andRoleIdIn(request.getRoleIds());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        Map<String, List<String>> existUserRelations = userRoleRelations.stream().collect(
                Collectors.groupingBy(UserRoleRelation::getUserId, Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        // 比较用户组是否已经存在, 如果不存在则添加
        List<UserRoleRelation> relations = new ArrayList<>();
        request.getUserIds().forEach(userId -> {
            AtomicBoolean isLog = new AtomicBoolean(false);
            // 追加的用户组ID, 操作记录使用
            List<String> roleIds = new ArrayList<>();
            request.getRoleIds().forEach(roleId -> {
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
                relation.setOrganizationId(project.getOrganizationId());
                relations.add(relation);
                isLog.set(true);
                roleIds.add(roleId);
            });
            // 成员添加操作记录
            if (isLog.get()) {
                UserRoleExample roleExample = new UserRoleExample();
                roleExample.createCriteria().andIdIn(roleIds);
                List<UserRole> userRoles = userRoleMapper.selectByExample(roleExample);
                // 追加了哪些用户组
                setLog(request.getProjectId(), userId, currentUserId, operationType, path, HttpMethodConstants.POST.name(), null, userRoles, logs);
            }
        });
        if (!CollectionUtils.isEmpty(relations)) {
            userRoleRelationMapper.batchInsert(relations);
        }
        // 操作记录
        operationLogService.batchAdd(logs);
    }

    /**
     * 批量移除成员(项目)
     *
     * @param request 请求参数
     */
    public void batchRemove(ProjectMemberBatchDeleteRequest request, String currentUserId) {
        // 操作记录
        List<LogDTO> logs = new ArrayList<>();
        // 项目不存在, 则不移除
        checkProjectExist(request.getProjectId());
        // 批量移除成员, 则移除该成员在该项目下的所有用户组
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(request.getProjectId())
                .andUserIdIn(request.getUserIds());
        userRoleRelationMapper.deleteByExample(example);
        // 操作记录
        request.getUserIds().forEach(userId -> {
            // 操作记录
            setLog(request.getProjectId(), userId, currentUserId, OperationLogType.DELETE.name(), "/project/member/remove", HttpMethodConstants.GET.name(), null, null, logs);
        });
        operationLogService.batchAdd(logs);
    }

    /**
     * 查看项目是否存在
     *
     * @param projectId 项目ID
     */
    private Project checkProjectExist(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            throw new MSException(Translator.get("project_not_exist"));
        }
        return project;
    }

    /**
     * 查看用户或用户组是否存在
     *
     * @param userId 用户ID
     * @param roleId 用户组ID
     * @return 是否存在
     */
    private boolean isUserOrRoleNotExist(String userId, String roleId) {
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andDeletedEqualTo(false);
        return CollectionUtils.isEmpty(userMapper.selectByExample(example)) || userRoleMapper.selectByPrimaryKey(roleId) == null;
    }

    /**
     * 操作记录
     *
     * @param projectId    项目ID
     * @param memberId     成员ID
     * @param createUserId 创建用户
     * @param type         操作类型
     * @param path         路径
     * @param method       请求方法
     * @param logs         日志集合
     */
    private void setLog(String projectId, String memberId, String createUserId, String type, String path, String method, Object originalVal, Object modifiedVal, List<LogDTO> logs) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        User user = userMapper.selectByPrimaryKey(memberId);
        LogDTO dto = new LogDTO(
                projectId,
                project.getOrganizationId(),
                memberId,
                createUserId,
                type,
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                user.getName());
        dto.setPath(path);
        dto.setMethod(method);
        dto.setOriginalValue(JSON.toJSONBytes(originalVal));
        dto.setModifiedValue(JSON.toJSONBytes(modifiedVal));
        logs.add(dto);
    }
}
