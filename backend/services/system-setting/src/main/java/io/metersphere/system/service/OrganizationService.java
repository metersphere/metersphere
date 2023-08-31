package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.service.BaseUserService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.dto.*;
import io.metersphere.system.mapper.*;
import io.metersphere.system.request.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author song-cc-rock
 * 组织功能
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationService {

    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private SystemProjectService systemProjectService;
    @Resource
    private UserRolePermissionMapper userRolePermissionMapper;
    @Resource
    private PluginOrganizationService pluginOrganizationService;
    @Resource
    private BaseUserService baseUserService;

    private static final String ADD_MEMBER_PATH = "/system/organization/add-member";
    private static final String REMOVE_MEMBER_PATH = "/system/organization/remove-member";

    /**
     * 分页获取系统下组织列表
     *
     * @param organizationRequest 请求参数
     * @return 组织集合
     */
    public List<OrganizationDTO> list(OrganizationRequest organizationRequest) {
        List<OrganizationDTO> organizationDTOS = extOrganizationMapper.list(organizationRequest);
        List<OrganizationDTO> organizations = buildUserInfo(organizationDTOS);
        return buildOrgAdminInfo(organizations);
    }

    /**
     * 获取系统下组织下拉选项
     *
     * @return 组织下拉选项集合
     */
    public List<OptionDTO> listAll() {
        List<OrganizationDTO> organizations = extOrganizationMapper.listAll();
        return organizations.stream().map(o -> new OptionDTO(o.getId(), o.getName())).toList();
    }

    /**
     * 分页获取组织成员列表
     *
     * @param request 请求参数
     * @return 组织成员集合
     */
    public List<UserExtend> getMemberListBySystem(OrganizationRequest request) {
        return extOrganizationMapper.listMember(request);
    }

    public void deleteOrganization(String organizationId) {
        List<LogDTO> logs = new ArrayList<>();
        Organization organization = organizationMapper.selectByPrimaryKey(organizationId);
        // 删除项目
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(organization.getId());
        List<Project> projects = projectMapper.selectByExample(projectExample);
        if (CollectionUtils.isNotEmpty(projects)) {
            systemProjectService.deleteProject(projects);
        }
        // 删除用户组, 用户组关系, 用户组权限
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andScopeIdEqualTo(organization.getId());
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        userRoleMapper.deleteByExample(userRoleExample);
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(organization.getId());
        userRoleRelationMapper.deleteByExample(userRoleRelationExample);
        if (CollectionUtils.isNotEmpty(userRoles)) {
            List<String> roleIds = userRoles.stream().map(UserRole::getId).toList();
            UserRolePermissionExample userRolePermissionExample = new UserRolePermissionExample();
            userRolePermissionExample.createCriteria().andRoleIdIn(roleIds);
            userRolePermissionMapper.deleteByExample(userRolePermissionExample);
        }
        // 删除组织和插件的关联关系
        pluginOrganizationService.deleteByOrgId(organizationId);

        // TODO: 删除环境组, 删除定时任务

        // 删除组织
        organizationMapper.deleteByPrimaryKey(organizationId);
        // 操作记录
        setLog(organizationId, "system", OperationLogType.DELETE.name(), organization.getName(), null, projects, null, logs);
        operationLogService.batchAdd(logs);
    }

    /**
     * 系统-组织-添加成员
     *
     * @param organizationMemberRequest 请求参数
     * @param createUserId              创建人ID
     */
    public void addMemberBySystem(OrganizationMemberRequest organizationMemberRequest, String createUserId) {
        List<LogDTO> logs = new ArrayList<>();
        OrganizationMemberBatchRequest batchRequest = new OrganizationMemberBatchRequest();
        batchRequest.setOrganizationIds(List.of(organizationMemberRequest.getOrganizationId()));
        batchRequest.setUserIds(organizationMemberRequest.getUserIds());
        addMemberBySystem(batchRequest, createUserId);
        // 添加日志
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(batchRequest.getUserIds());
        List<User> users = userMapper.selectByExample(example);
        Organization organization = organizationMapper.selectByPrimaryKey(organizationMemberRequest.getOrganizationId());
        setLog(organizationMemberRequest.getOrganizationId(), createUserId, OperationLogType.UPDATE.name(), organization.getName(), ADD_MEMBER_PATH, null, users, logs);
        operationLogService.batchAdd(logs);
    }

    /**
     * 组织添加成员公共方法(N个组织添加N个成员)
     *
     * @param batchRequest 请求参数 [organizationIds 组织集合, userIds 成员集合]
     * @param createUserId 创建人ID
     */
    public void addMemberBySystem(OrganizationMemberBatchRequest batchRequest, String createUserId) {
        checkOrgExistByIds(batchRequest.getOrganizationIds());
        Map<String, User> userMap = checkUserExist(batchRequest.getUserIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        batchRequest.getOrganizationIds().forEach(organizationId -> {
            for (String userId : batchRequest.getUserIds()) {
                if (userMap.get(userId) == null) {
                    throw new MSException(Translator.get("user.not.exist") + ", id: " + userId);
                }
                //组织用户关系已存在, 不再重复添加
                UserRoleRelationExample example = new UserRoleRelationExample();
                example.createCriteria().andSourceIdEqualTo(organizationId).andUserIdEqualTo(userId);
                if (userRoleRelationMapper.countByExample(example) > 0) {
                    continue;
                }
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setId(UUID.randomUUID().toString());
                userRoleRelation.setUserId(userId);
                userRoleRelation.setSourceId(organizationId);
                userRoleRelation.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
                userRoleRelation.setCreateTime(System.currentTimeMillis());
                userRoleRelation.setCreateUser(createUserId);
                userRoleRelation.setOrganizationId(organizationId);
                userRoleRelations.add(userRoleRelation);
            }
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.batchInsert(userRoleRelations);
        }
    }
    /**
     * 删除组织成员
     *
     * @param organizationId 组织ID
     * @param userId         成员ID
     */
    public void removeMember(String organizationId, String userId) {
        List<LogDTO> logs = new ArrayList<>();
        checkOrgExistById(organizationId);
        //删除组织下项目与成员的关系
        List<String> projectIds = getProjectIds(organizationId);
        if (CollectionUtils.isNotEmpty(projectIds)) {
            UserRoleRelationExample example = new UserRoleRelationExample();
            example.createCriteria().andUserIdEqualTo(userId).andSourceIdIn(projectIds);
            userRoleRelationMapper.deleteByExample(example);
        }
        //删除组织与成员的关系
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(organizationId);
        userRoleRelationMapper.deleteByExample(example);
        // 操作记录
        User user = userMapper.selectByPrimaryKey(userId);
        Organization organization = organizationMapper.selectByPrimaryKey(organizationId);
        setLog(organizationId, userId, OperationLogType.UPDATE.name(), organization.getName(), REMOVE_MEMBER_PATH, user, null, logs);
        operationLogService.batchAdd(logs);
    }

    /**
     * 获取系统默认组织
     *
     * @return 组织信息
     */
    public OrganizationDTO getDefault() {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andNumEqualTo(100001L);
        List<Organization> organizations = organizationMapper.selectByExample(example);
        Organization organization = organizations.get(0);
        BeanUtils.copyBean(organizationDTO, organization);
        return organizationDTO;
    }

    /**
     * 组织级别获取组织成员
     *
     * @param organizationRequest 请求参数
     * @return 组织成员集合
     */
    public List<OrgUserExtend> getMemberListByOrg(OrganizationRequest organizationRequest) {
        //根据组织ID获取所有组织用户关系表
        String organizationId = organizationRequest.getOrganizationId();
        List<OrgUserExtend> orgUserExtends = extOrganizationMapper.listMemberByOrg(organizationRequest);
        if (CollectionUtils.isEmpty(orgUserExtends)) {
            return new ArrayList<>();
        }
        Map<String, OrgUserExtend> userMap = orgUserExtends.stream().collect(Collectors.toMap(OrgUserExtend::getId, user -> user));
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
        List<Project> projectsList = projectMapper.selectByExample(projectExample);
        List<String> projectIdList = projectsList.stream().map(Project::getId).toList();
        Map<String, String> projectIdNameMap = projectsList.stream().collect(Collectors.toMap(Project::getId, Project::getName));
        //根据用户id获取所有与该用户有关的当前组织以及组织下的项目关系表
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        List<String> sourceIds = new ArrayList<>(projectIdList);
        sourceIds.add(organizationId);
        userRoleRelationExample.createCriteria().andUserIdIn(new ArrayList<>(userMap.keySet())).andSourceIdIn(sourceIds);
        userRoleRelationExample.setOrderByClause("create_time desc");
        List<UserRoleRelation> userRoleRelationsByUsers = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //根据关系表查询出用户的关联组织和用户组
        Map<String, List<IdNameStructureDTO>> userIdprojectIdMap = new HashMap<>();
        Map<String, Set<String>> userIdRoleIdMap = new HashMap<>();
        for (UserRoleRelation userRoleRelationsByUser : userRoleRelationsByUsers) {
            String sourceId = userRoleRelationsByUser.getSourceId();
            String roleId = userRoleRelationsByUser.getRoleId();
            String userId = userRoleRelationsByUser.getUserId();
            List<IdNameStructureDTO> pIdNameList = userIdprojectIdMap.get(userId);
            if (CollectionUtils.isEmpty(pIdNameList)) {
                pIdNameList = new ArrayList<>();
            }
            String projectName = projectIdNameMap.get(sourceId);
            if (StringUtils.isNotBlank(projectName)) {
                IdNameStructureDTO idNameStructureDTO = new IdNameStructureDTO();
                idNameStructureDTO.setId(sourceId);
                idNameStructureDTO.setName(projectName);
                pIdNameList.add(idNameStructureDTO);
            }
            userIdprojectIdMap.put(userId, pIdNameList);

            //只显示组织级别的用户组
            if (StringUtils.equals(sourceId, organizationId)) {
                Set<String> roleIds = userIdRoleIdMap.get(userId);
                if (CollectionUtils.isEmpty(roleIds)) {
                    roleIds = new HashSet<>();
                }
                roleIds.add(roleId);
                userIdRoleIdMap.put(userId, roleIds);
            }
        }
        for (OrgUserExtend orgUserExtend : orgUserExtends) {
            List<IdNameStructureDTO> projectList = userIdprojectIdMap.get(orgUserExtend.getId());
            if (CollectionUtils.isNotEmpty(projectList)) {
                orgUserExtend.setProjectIdNameMap(projectList);
            }
            Set<String> userRoleIds = userIdRoleIdMap.get(orgUserExtend.getId());
            UserRoleExample userRoleExample = new UserRoleExample();
            userRoleExample.createCriteria().andIdIn(new ArrayList<>(userRoleIds));
            List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
            List<IdNameStructureDTO> userRoleList = new ArrayList<>();
            setUserRoleList(userRoleList, userRoles);
            orgUserExtend.setUserRoleIdNameMap(userRoleList);
        }
        return orgUserExtends;
    }

    public List<OrganizationProjectOptionsDTO> getOrganizationOptions() {
        return extOrganizationMapper.selectOrganizationOptions();
    }

    public void addMemberByOrg(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId) {
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        checkOrgExistById(organizationId);
        Map<String, User> userMap = checkUserExist(organizationMemberExtendRequest.getMemberIds());
        Map<String, UserRole> userRoleMap = checkUseRoleExist(organizationMemberExtendRequest.getUserRoleIds(), organizationId);
        setRelationByMemberAndGroupIds(organizationMemberExtendRequest, createUserId, userMap, userRoleMap, true);
    }

    private void setRelationByMemberAndGroupIds(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId, Map<String, User> userMap, Map<String, UserRole> userRoleMap, boolean add) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserRoleRelationMapper userRoleRelationMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        List<LogDTO> logDTOList = new ArrayList<>();
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        organizationMemberExtendRequest.getMemberIds().forEach(memberId -> {
            if (userMap.get(memberId) == null) {
                throw new MSException("id:" + memberId + Translator.get("user.not.exist"));
            }
            organizationMemberExtendRequest.getUserRoleIds().forEach(userRoleId -> {
                if (userRoleMap.get(userRoleId) != null) {
                    //过滤已存在的关系
                    UserRoleRelationExample example = new UserRoleRelationExample();
                    example.createCriteria().andSourceIdEqualTo(organizationId).andUserIdEqualTo(memberId).andRoleIdEqualTo(userRoleId);
                    List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
                    if (CollectionUtils.isEmpty(userRoleRelations)) {
                        UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, organizationId, userRoleId);
                        userRoleRelation.setOrganizationId(organizationId);
                        userRoleRelationMapper.insert(userRoleRelation);
                        //add Log
                        String path = add ? "/organization/add-member" : "/organization/role/update-member";
                        String type = add ? OperationLogType.ADD.name() : OperationLogType.UPDATE.name();
                        LogDTO dto = new LogDTO(
                                OperationLogConstants.ORGANIZATION,
                                organizationId,
                                memberId,
                                createUserId,
                                type,
                                OperationLogModule.SETTING_ORGANIZATION_MEMBER,
                                userMap.get(memberId).getName());
                        setLog(dto, path, logDTOList, userRoleRelation);
                    }
                }
            });
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        //写入操作日志
        operationLogService.batchAdd(logDTOList);
    }

    private static void setLog(LogDTO dto, String path, List<LogDTO> logDTOList, Object originalValue) {
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        logDTOList.add(dto);
    }

    /**
     * 添加组织成员至用户组
     *
     * @param organizationMemberExtendRequest 请求参数
     * @param userId                          创建人ID
     */
    public void addMemberRole(OrganizationMemberExtendRequest organizationMemberExtendRequest, String userId) {
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        checkOrgExistById(organizationId);
        Map<String, User> userMap = checkUserExist(organizationMemberExtendRequest.getMemberIds());
        Map<String, UserRole> userRoleMap = checkUseRoleExist(organizationMemberExtendRequest.getUserRoleIds(), organizationId);
        //在新增组织成员与用户组和组织的关系
        setRelationByMemberAndGroupIds(organizationMemberExtendRequest, userId, userMap, userRoleMap, false);
    }

    public void addMemberToProject(OrgMemberExtendProjectRequest orgMemberExtendProjectRequest, String userId) {
        String requestOrganizationId = orgMemberExtendProjectRequest.getOrganizationId();
        checkOrgExistById(requestOrganizationId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserRoleRelationMapper userRoleRelationMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        List<LogDTO> logDTOList = new ArrayList<>();
        List<String> projectIds = orgMemberExtendProjectRequest.getProjectIds();
        //用户不在当前组织内过掉
        Map<String, User> userMap = checkUserExist(orgMemberExtendProjectRequest.getMemberIds());
        List<String> userIds = userMap.values().stream().map(User::getId).toList();
        userIds.forEach(memberId -> {
            projectIds.forEach(projectId -> {
                //过滤已存在的关系
                UserRoleRelationExample example = new UserRoleRelationExample();
                example.createCriteria().andSourceIdEqualTo(projectId).andUserIdEqualTo(memberId);
                List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(userRoleRelations)) {
                    UserRoleRelation userRoleRelation = buildUserRoleRelation(userId, memberId, projectId, InternalUserRole.PROJECT_MEMBER.getValue());
                    userRoleRelation.setOrganizationId(orgMemberExtendProjectRequest.getOrganizationId());
                    userRoleRelationMapper.insert(userRoleRelation);
                    //add Log
                    LogDTO dto = new LogDTO(
                            projectId,
                            requestOrganizationId,
                            memberId,
                            userId,
                            OperationLogType.ADD.name(),
                            OperationLogModule.PROJECT_PROJECT_MEMBER,
                            "");
                    setLog(dto, "/organization/project/add-member", logDTOList, userRoleRelation);
                }
            });
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        //写入操作日志
        operationLogService.batchAdd(logDTOList);

    }

    /**
     * 删除组织用户日志
     *
     * @return 日志内容
     */
    public List<LogDTO> batchDelLog(String organizationId, String userId) {
        List<String> projectIds = getProjectIds(organizationId);
        UserRoleRelationExample example = new UserRoleRelationExample();
        if (CollectionUtils.isEmpty(projectIds)) {
            return null;
        }
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdIn(projectIds);
        List<UserRoleRelation> userRoleWidthProjectRelations = userRoleRelationMapper.selectByExample(example);
        example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleWidthOrgRelations = userRoleRelationMapper.selectByExample(example);
        List<LogDTO> dtoList = new ArrayList<>();
        User user = userMapper.selectByPrimaryKey(userId);
        //记录项目日志
        for (UserRoleRelation userRoleWidthProjectRelation : userRoleWidthProjectRelations) {
            LogDTO dto = new LogDTO(
                    userRoleWidthProjectRelation.getSourceId(),
                    organizationId,
                    userId,
                    userRoleWidthProjectRelation.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                    user.getName());

            dto.setPath("/organization/remove-member/{organizationId}/{userId}");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(userRoleWidthProjectRelation));
            dtoList.add(dto);
        }
        //记录组织日志
        for (UserRoleRelation userRoleWidthOrgRelation : userRoleWidthOrgRelations) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    organizationId,
                    userRoleWidthOrgRelation.getId(),
                    userRoleWidthOrgRelation.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_MEMBER,
                    user.getName());

            dto.setPath("/organization/remove-member/{organizationId}/{userId}");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(userRoleWidthOrgRelation));
            dtoList.add(dto);
        }
        return dtoList;
    }

    private List<String> getProjectIds(String organizationId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
        List<Project> projects = projectMapper.selectByExample(projectExample);
        if (CollectionUtils.isNotEmpty(projects)) {
            return projects.stream().map(Project::getId).toList();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 更新用户
     *
     * @param organizationMemberUpdateRequest 请求参数
     * @param createUserId                    创建人ID
     */
    public void updateMember(OrganizationMemberUpdateRequest organizationMemberUpdateRequest, String createUserId) {
        String organizationId = organizationMemberUpdateRequest.getOrganizationId();
        //校验组织是否存在
        checkOrgExistById(organizationId);
        //校验用户是否存在
        String memberId = organizationMemberUpdateRequest.getMemberId();
        User user = userMapper.selectByPrimaryKey(memberId);
        if (user == null) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        //校验成员是否是当前组织的成员
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(memberId).andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            throw new MSException(Translator.get("organization_member_not_exist"));
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        List<LogDTO> logDTOList = new ArrayList<>();
        //更新用户组
        List<String> userRoleIds = organizationMemberUpdateRequest.getUserRoleIds();
        updateUserRoleRelation(createUserId, organizationId, user, userRoleIds, sqlSession, logDTOList);
        //更新项目
        List<String> projectIds = organizationMemberUpdateRequest.getProjectIds();
        if (CollectionUtils.isNotEmpty(projectIds)) {
            updateProjectUserRelation(createUserId, organizationId, user, projectIds, sqlSession, logDTOList);
        } else {
            ProjectExample projectExample = new ProjectExample();
            projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
            List<Project> projects = projectMapper.selectByExample(projectExample);
            if (CollectionUtils.isNotEmpty(projects)) {
                List<String> projectInDBInOrgIds = projects.stream().map(Project::getId).collect(Collectors.toList());
                userRoleRelationExample = new UserRoleRelationExample();
                userRoleRelationExample.createCriteria().andUserIdEqualTo(memberId).andSourceIdIn(projectInDBInOrgIds);
                userRoleRelationMapper.deleteByExample(userRoleRelationExample);
                //add Log
                for (String projectInDBInOrgId : projectInDBInOrgIds) {
                    String path = "/organization/update-member";
                    LogDTO dto = new LogDTO(
                            projectInDBInOrgId,
                            organizationId,
                            memberId,
                            createUserId,
                            OperationLogType.UPDATE.name(),
                            OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                            user.getName());
                    setLog(dto, path, logDTOList, "");
                }

            }
        }

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        //写入操作日志
        operationLogService.batchAdd(logDTOList);
    }

    private void updateProjectUserRelation(String createUserId, String organizationId, User user, List<String> projectIds, SqlSession sqlSession, List<LogDTO> logDTOList) {
        Map<String, Project> projectMap = checkProjectExist(projectIds, organizationId);
        List<String> projectInDBInOrgIds = projectMap.values().stream().map(Project::getId).toList();
        //删除旧的关系
        String memberId = user.getId();
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
        List<Project> projects = projectMapper.selectByExample(projectExample);
        List<String> projectIdsAll = projects.stream().map(Project::getId).toList();
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(memberId).andSourceIdIn(projectIdsAll);
        userRoleRelationMapper.deleteByExample(userRoleRelationExample);
        UserRoleRelationMapper userRoleRelationMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        projectInDBInOrgIds.forEach(projectId -> {
            UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, projectId, InternalUserRole.PROJECT_MEMBER.getValue());
            userRoleRelation.setOrganizationId(organizationId);
            userRoleRelationMapper.insert(userRoleRelation);
            //add Log
            String path = "/organization/update-member";
            LogDTO dto = new LogDTO(
                    projectId,
                    organizationId,
                    memberId,
                    createUserId,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                    user.getName());
            setLog(dto, path, logDTOList, userRoleRelation);
        });
    }

    private UserRoleRelation buildUserRoleRelation(String createUserId, String memberId, String sourceId, String roleId) {
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setId(UUID.randomUUID().toString());
        userRoleRelation.setUserId(memberId);
        userRoleRelation.setSourceId(sourceId);
        userRoleRelation.setRoleId(roleId);
        userRoleRelation.setCreateTime(System.currentTimeMillis());
        userRoleRelation.setCreateUser(createUserId);
        return userRoleRelation;
    }

    private void updateUserRoleRelation(String createUserId, String organizationId, User user, List<String> userRoleIds, SqlSession sqlSession, List<LogDTO> logDTOList) {
        //检查用户组是否是组织级别用户组
        String memberId = user.getId();
        Map<String, UserRole> userRoleMap = checkUseRoleExist(userRoleIds, organizationId);
        List<String> userRoleInDBInOrgIds = userRoleMap.values().stream().map(UserRole::getId).toList();
        //删除旧的关系
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(memberId).andSourceIdEqualTo(organizationId);
        userRoleRelationMapper.deleteByExample(userRoleRelationExample);
        UserRoleRelationMapper userRoleRelationMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        userRoleInDBInOrgIds.forEach(userRoleId -> {
            UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, organizationId, userRoleId);
            userRoleRelation.setOrganizationId(organizationId);
            userRoleRelationMapper.insert(userRoleRelation);
            //add Log
            String path = "/organization/update-member";
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    organizationId,
                    memberId,
                    createUserId,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_MEMBER,
                    user.getName());
            setLog(dto, path, logDTOList, userRoleRelation);
        });
    }

    /**
     * 获取当前组织下的所有项目
     *
     * @param organizationId 组织ID
     * @return 项目列表
     */
    public List<IdNameStructureDTO> getProjectList(String organizationId) {
        //校验组织是否存在
        checkOrgExistById(organizationId);
        List<IdNameStructureDTO> projectList = new ArrayList<>();
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
        List<Project> projects = projectMapper.selectByExample(projectExample);
        for (Project project : projects) {
            IdNameStructureDTO idNameStructureDTO = new IdNameStructureDTO();
            idNameStructureDTO.setId(project.getId());
            idNameStructureDTO.setName(project.getName());
            projectList.add(idNameStructureDTO);
        }
        return projectList;
    }

    /**
     * 获取当前组织下的所有自定义用户组以及组织级别的用户组
     *
     * @param organizationId 组织ID
     * @return 用户组列表
     */
    public List<IdNameStructureDTO> getUserRoleList(String organizationId) {
        //校验组织是否存在
        checkOrgExistById(organizationId);
        List<String> scopeIds = Arrays.asList(UserRoleEnum.GLOBAL.toString(), organizationId);
        List<IdNameStructureDTO> userRoleList = new ArrayList<>();
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andTypeEqualTo(UserRoleType.ORGANIZATION.toString()).andScopeIdIn(scopeIds);
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        setUserRoleList(userRoleList, userRoles);
        return userRoleList;
    }

    private static void setUserRoleList(List<IdNameStructureDTO> userRoleList, List<UserRole> userRoles) {
        for (UserRole userRole : userRoles) {
            IdNameStructureDTO idNameStructureDTO = new IdNameStructureDTO();
            idNameStructureDTO.setId(userRole.getId());
            idNameStructureDTO.setName(userRole.getName());
            userRoleList.add(idNameStructureDTO);
        }
    }

    /**
     * 获取不在当前组织的所有用户
     *
     * @param organizationId 组织ID
     * @return 用户列表
     */
    public List<IdNameStructureDTO> getUserList(String organizationId) {
        //校验组织是否存在
        checkOrgExistById(organizationId);
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        if (CollectionUtils.isNotEmpty(userIds)) {
            criteria.andIdNotIn(userIds);
        }
        criteria.andDeletedEqualTo(false);
        List<User> users = userMapper.selectByExample(userExample);
        List<IdNameStructureDTO> userList = new ArrayList<>();
        for (User user : users) {
            IdNameStructureDTO idNameStructureDTO = new IdNameStructureDTO();
            idNameStructureDTO.setId(user.getId());
            idNameStructureDTO.setName(user.getName());
            userList.add(idNameStructureDTO);
        }
        return userList;
    }

    /**
     * 检查组织是否存在
     *
     * @param organizationIds 组织ID集合
     */
    private void checkOrgExistByIds(List<String> organizationIds) {
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andIdIn(organizationIds);
        if (organizationMapper.countByExample(example) < organizationIds.size()) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
    }

    /**
     * 检查组织是否存在
     *
     * @param organizationId 组织ID
     */
    private void checkOrgExistById(String organizationId) {
        Organization organization = organizationMapper.selectByPrimaryKey(organizationId);
        if (organization == null) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
    }

    /**
     * 检查组织级别的用户组是否存在
     *
     * @param userRoleIds    用户组ID集合
     * @param organizationId 组织ID
     * @return 用户组集合
     */
    private Map<String, UserRole> checkUseRoleExist(List<String> userRoleIds, String organizationId) {
        UserRoleExample userRoleExample = new UserRoleExample();
        List<String> scopeIds = Arrays.asList(UserRoleEnum.GLOBAL.toString(), organizationId);
        userRoleExample.createCriteria().andIdIn(userRoleIds).andTypeEqualTo(UserRoleType.ORGANIZATION.toString()).andScopeIdIn(scopeIds);
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        if (CollectionUtils.isEmpty(userRoles)) {
            throw new MSException(Translator.get("user_role_not_exist"));
        }
        return userRoles.stream().collect(Collectors.toMap(UserRole::getId, user -> user));

    }

    /**
     * 检查用户是否存在
     *
     * @param userIds 成员ID集合
     * @return 用户集合
     */
    private Map<String, User> checkUserExist(List<String> userIds) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);
        if (CollectionUtils.isEmpty(users)) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        return users.stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    /**
     * 检查项目是否存在
     *
     * @param projectIds     项目ID集合
     * @param organizationId 组织ID
     * @return 项目集合
     */
    private Map<String, Project> checkProjectExist(List<String> projectIds, String organizationId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds).andOrganizationIdEqualTo(organizationId);
        List<Project> projects = projectMapper.selectByExample(projectExample);
        if (CollectionUtils.isEmpty(projects)) {
            throw new MSException(Translator.get("project_not_exist"));
        }
        return projects.stream().collect(Collectors.toMap(Project::getId, project -> project));
    }

    /**
     * 处理组织管理员信息
     *
     * @param organizationDTOS 组织集合
     * @return 组织列表
     */
    private List<OrganizationDTO> buildOrgAdminInfo(List<OrganizationDTO> organizationDTOS) {
        if (CollectionUtils.isEmpty(organizationDTOS)) {
            return organizationDTOS;
        }
        organizationDTOS.forEach(organizationDTO -> {
            List<User> orgAdminList = extOrganizationMapper.getOrgAdminList(organizationDTO.getId());
            organizationDTO.setOrgAdmins(orgAdminList);
            List<String> userIds = orgAdminList.stream().map(User::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userIds) && userIds.contains(organizationDTO.getCreateUser())) {
                organizationDTO.setOrgCreateUserIsAdmin(true);
            }
        });
        return organizationDTOS;
    }

    /**
     * 设置用户信息
     *
     * @param organizationDTOS 组织集合
     * @return 组织集合
     */
    private List<OrganizationDTO> buildUserInfo(List<OrganizationDTO> organizationDTOS) {
        List<String> userIds = new ArrayList<>();
        userIds.addAll(organizationDTOS.stream().map(OrganizationDTO::getCreateUser).toList());
        userIds.addAll(organizationDTOS.stream().map(OrganizationDTO::getUpdateUser).toList());
        userIds.addAll(organizationDTOS.stream().map(OrganizationDTO::getDeleteUser).toList());
        Map<String, String> userMap = baseUserService.getUserNameMap(userIds.stream().distinct().toList());
        organizationDTOS.forEach(organizationDTO -> {
            organizationDTO.setCreateUser(userMap.get(organizationDTO.getCreateUser()));
            organizationDTO.setDeleteUser(userMap.get(organizationDTO.getDeleteUser()));
            organizationDTO.setUpdateUser(userMap.get(organizationDTO.getUpdateUser()));
        });
        return organizationDTOS;
    }

    /**
     * 设置操作日志
     *
     * @param organizationId 组织ID
     * @param createUser     创建人
     * @param type           操作类型
     * @param content        操作内容
     * @param path           请求路径
     * @param originalValue  原始值
     * @param modifiedValue  修改值
     * @param logs           日志集合
     */
    private void setLog(String organizationId, String createUser, String type, String content, String path, Object originalValue, Object modifiedValue, List<LogDTO> logs) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                organizationId,
                createUser,
                type,
                OperationLogModule.SETTING_SYSTEM_ORGANIZATION,
                content);
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        dto.setModifiedValue(JSON.toJSONBytes(modifiedValue));
        logs.add(dto);
    }

    public LinkedHashMap<Organization, List<Project>> getOrgProjectMap() {
        ProjectExample projectExample = new ProjectExample();
        projectExample.setOrderByClause("name asc");
        List<Project> allProject = projectMapper.selectByExample(projectExample);
        if (CollectionUtils.isNotEmpty(allProject)) {
            LinkedHashMap<Organization, List<Project>> returnMap = new LinkedHashMap<>();
            OrganizationExample orgExample = new OrganizationExample();
            orgExample.createCriteria().andIdIn(allProject.stream().map(Project::getOrganizationId).distinct().collect(Collectors.toList()));
            orgExample.setOrderByClause("name asc");
            List<Organization> orgList = organizationMapper.selectByExample(orgExample);
            for (Organization org : orgList) {
                List<Project> projectsInOrg = new ArrayList<>();
                for (Project project : allProject) {
                    if (StringUtils.equals(project.getOrganizationId(), org.getId())) {
                        projectsInOrg.add(project);
                    }
                }
                allProject.remove(projectsInOrg);
                returnMap.put(org, projectsInOrg);
            }
            return returnMap;
        } else {
            return new LinkedHashMap<>();
        }
    }

    public Map<String, Long> getTotal(String organizationId) {
        Map<String, Long> total = new HashMap<>();
        ProjectExample projectExample = new ProjectExample();
        OrganizationExample organizationExample = new OrganizationExample();
        if (StringUtils.isBlank(organizationId)) {
            // 统计所有项目
            total.put("projectTotal", projectMapper.countByExample(projectExample));
            total.put("organizationTotal", organizationMapper.countByExample(organizationExample));
        } else {
            // 统计组织下的项目
            projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
            total.put("projectTotal", projectMapper.countByExample(projectExample));
            total.put("organizationTotal", 1L);
        }
        return total;
    }
}
