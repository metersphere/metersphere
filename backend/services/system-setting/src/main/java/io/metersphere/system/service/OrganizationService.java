package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.*;
import io.metersphere.system.dto.request.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.dto.user.UserRoleOptionDto;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.*;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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
    private BaseUserMapper baseUserMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;
    @Resource
    private ExtSystemOrgProjectMapper extSystemOrgProjectMapper;
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
    private UserLoginService userLoginService;
    @Resource
    private BaseTemplateService baseTemplateService;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;

    private static final String ADD_MEMBER_PATH = "/system/organization/add-member";
    private static final String REMOVE_MEMBER_PATH = "/system/organization/remove-member";
    public static final Integer DEFAULT_REMAIN_DAY_COUNT = 30;
    private static final Long DEFAULT_ORGANIZATION_NUM = 100001L;
    @Resource
    private ExtUserRoleRelationMapper extUserRoleRelationMapper;

    /**
     * 分页获取系统下组织列表
     *
     * @param organizationRequest 请求参数
     * @return 组织集合
     */
    public List<OrganizationDTO> list(OrganizationRequest organizationRequest, String currentUser) {
        List<OrganizationDTO> organizationDTOS = extOrganizationMapper.list(organizationRequest);
        if (CollectionUtils.isEmpty(organizationDTOS)) {
            return new ArrayList<>();
        }
        List<OrganizationDTO> organizations = buildOrgAdminInfo(organizationDTOS);
        return buildExtraInfo(organizations, currentUser);
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
     * 更新组织名称
     *
     * @param organizationDTO 组织请求参数
     */
    public void updateName(OrganizationDTO organizationDTO) {
        checkOrganizationNotExist(organizationDTO.getId());
        checkOrganizationExist(organizationDTO);
        organizationDTO.setUpdateTime(System.currentTimeMillis());
        organizationDTO.setCreateUser(null);
        organizationDTO.setCreateTime(null);
        organizationMapper.updateByPrimaryKeySelective(organizationDTO);
    }

    /**
     * 更新组织
     *
     * @param organizationDTO 组织请求参数
     */
    public void update(OrganizationDTO organizationDTO) {
        checkOrganizationNotExist(organizationDTO.getId());
        checkOrganizationExist(organizationDTO);
        organizationDTO.setUpdateTime(System.currentTimeMillis());
        organizationDTO.setCreateUser(null);
        organizationDTO.setCreateTime(null);
        organizationMapper.updateByPrimaryKeySelective(organizationDTO);

        // 新增的组织管理员ID
        List<String> addOrgAdmins = organizationDTO.getUserIds();
        // 旧的组织管理员ID
        List<String> oldOrgAdmins = getOrgAdminIds(organizationDTO.getId());
        // 需要新增组织管理员ID
        List<String> addIds = addOrgAdmins.stream().filter(addOrgAdmin -> !oldOrgAdmins.contains(addOrgAdmin)).toList();
        // 需要删除的组织管理员ID
        List<String> deleteIds = oldOrgAdmins.stream().filter(oldOrgAdmin -> !addOrgAdmins.contains(oldOrgAdmin)).toList();
        // 添加组织管理员
        if (CollectionUtils.isNotEmpty(addIds)) {
            addIds.forEach(userId -> {
                // 添加组织管理员
                createAdmin(userId, organizationDTO.getId(), organizationDTO.getUpdateUser());
            });
        }
        // 删除组织管理员
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            UserRoleRelationExample deleteExample = new UserRoleRelationExample();
            deleteExample.createCriteria().andSourceIdEqualTo(organizationDTO.getId()).andRoleIdEqualTo(InternalUserRole.ORG_ADMIN.getValue()).andUserIdIn(deleteIds);
            userRoleRelationMapper.deleteByExample(deleteExample);
        }
    }

    /**
     * 删除组织
     *
     * @param organizationDeleteRequest 组织删除参数
     */
    public void delete(OrganizationDeleteRequest organizationDeleteRequest) {
        // 默认组织不允许删除
        checkOrgDefault(organizationDeleteRequest.getOrganizationId());
        checkOrganizationNotExist(organizationDeleteRequest.getOrganizationId());
        organizationDeleteRequest.setDeleteTime(System.currentTimeMillis());
        extOrganizationMapper.delete(organizationDeleteRequest);
    }

    /**
     * 恢复组织
     *
     * @param id 组织ID
     */
    public void recover(String id) {
        checkOrganizationNotExist(id);
        extOrganizationMapper.recover(id);
    }

    /**
     * 开启组织
     *
     * @param id 组织ID
     */
    public void enable(String id) {
        checkOrganizationNotExist(id);
        extOrganizationMapper.updateEnable(id, Boolean.TRUE);
    }

    /**
     * 结束组织
     *
     * @param id 组织ID
     */
    public void disable(String id) {
        checkOrganizationNotExist(id);
        extOrganizationMapper.updateEnable(id, Boolean.FALSE);
    }

    /**
     * 分页获取组织成员列表
     *
     * @param request 请求参数
     * @return 组织成员集合
     */
    public List<UserExtendDTO> getMemberListBySystem(OrganizationRequest request) {
        List<UserExtendDTO> userExtendDTOS = extOrganizationMapper.listMember(request);
        if (CollectionUtils.isNotEmpty(userExtendDTOS)) {
            List<String> userIds = userExtendDTOS.stream().map(UserExtendDTO::getId).toList();
            List<UserRoleOptionDto> userRole = extUserRoleRelationMapper.selectUserRoleByUserIds(userIds, request.getOrganizationId());
            Map<String, List<UserRoleOptionDto>> roleMap = userRole.stream().collect(Collectors.groupingBy(UserRoleOptionDto::getUserId));
            userExtendDTOS.forEach(user -> {
                if (roleMap.containsKey(user.getId())) {
                    user.setUserRoleList(roleMap.get(user.getId()));
                }
            });

        }
        return userExtendDTOS;
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

        // 删除组织模板和字段
        baseTemplateService.deleteByScopeId(organizationId);
        baseCustomFieldService.deleteByScopeId(organizationId);

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
        addMemberAndGroup(organizationMemberRequest, createUserId);
        // 添加日志
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(organizationMemberRequest.getUserIds());
        List<User> users = userMapper.selectByExample(example);
        List<String> nameList = users.stream().map(User::getName).collect(Collectors.toList());
        setLog(organizationMemberRequest.getOrganizationId(), createUserId, OperationLogType.ADD.name(), Translator.get("add") + Translator.get("organization_member_log") + ": " + StringUtils.join(nameList, ","), ADD_MEMBER_PATH, null, null, logs);
        operationLogService.batchAdd(logs);
    }

    /**
     * 系统-组织与项目-组织-添加成员（用户+用户组）
     *
     * @param request
     * @param createUserId
     */
    private void addMemberAndGroup(OrganizationMemberRequest request, String createUserId) {
        checkOrgExistByIds(List.of(request.getOrganizationId()));
        Map<String, User> userMap = checkUserExist(request.getUserIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        for (String userId : request.getUserIds()) {
            if (userMap.get(userId) == null) {
                throw new MSException(Translator.get("user.not.exist") + ", id: " + userId);
            }
            //组织用户关系已存在, 不再重复添加
            UserRoleRelationExample example = new UserRoleRelationExample();
            example.createCriteria().andSourceIdEqualTo(request.getOrganizationId()).andUserIdEqualTo(userId);
            if (userRoleRelationMapper.countByExample(example) > 0) {
                continue;
            }
            request.getUserRoleIds().forEach(userRoleId -> {
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setId(IDGenerator.nextStr());
                userRoleRelation.setUserId(userId);
                userRoleRelation.setSourceId(request.getOrganizationId());
                userRoleRelation.setRoleId(userRoleId);
                userRoleRelation.setCreateTime(System.currentTimeMillis());
                userRoleRelation.setCreateUser(createUserId);
                userRoleRelation.setOrganizationId(request.getOrganizationId());
                userRoleRelations.add(userRoleRelation);
            });
        }
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.batchInsert(userRoleRelations);
        }
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
                userRoleRelation.setId(IDGenerator.nextStr());
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
    public void removeMember(String organizationId, String userId, String currentUser) {
        List<LogDTO> logs = new ArrayList<>();
        checkOrgExistById(organizationId);
        //检查用户是不是最后一个管理员
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdNotEqualTo(userId)
                .andSourceIdEqualTo(organizationId)
                .andRoleIdEqualTo(InternalUserRole.ORG_ADMIN.getValue());
        if (userRoleRelationMapper.countByExample(userRoleRelationExample) == 0) {
            throw new MSException(Translator.get("keep_at_least_one_administrator"));
        }
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
        setLog(organizationId, currentUser, OperationLogType.DELETE.name(), Translator.get("delete") + Translator.get("organization_member_log") + ": " + user.getName(), REMOVE_MEMBER_PATH, user, null, logs);
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
        Organization organization = organizations.getFirst();
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
        //根据用户id获取所有与该用户有关的当前组织以及组织下的项目关系表
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdIn(new ArrayList<>(userMap.keySet())).andOrganizationIdEqualTo(organizationId);
        userRoleRelationExample.setOrderByClause("create_time desc");
        List<UserRoleRelation> userRoleRelationsByUsers = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //根据关系表查询出用户的关联组织和用户组
        Map<String, Set<String>> userIdRoleIdMap = new HashMap<>();
        Map<String, Set<String>> userIdProjectIdMap = new HashMap<>();
        Set<String> roleIdSet = new HashSet<>();
        Set<String> projectIdSet = new HashSet<>();
        for (UserRoleRelation userRoleRelationsByUser : userRoleRelationsByUsers) {
            String sourceId = userRoleRelationsByUser.getSourceId();
            String roleId = userRoleRelationsByUser.getRoleId();
            String userId = userRoleRelationsByUser.getUserId();
            //收集组织级别的用户组
            if (StringUtils.equals(sourceId, organizationId)) {
                getTargetIds(userIdRoleIdMap, roleIdSet, roleId, userId);
            }
            //收集项目id
            if (!StringUtils.equals(sourceId, organizationId)) {
                getTargetIds(userIdProjectIdMap, projectIdSet, sourceId, userId);
            }
        }
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andIdIn(new ArrayList<>(roleIdSet));
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);

        List<Project> projects = new ArrayList<>();
        if (projectIdSet.size() > 0) {
            ProjectExample projectExample = new ProjectExample();
            projectExample.createCriteria().andIdIn(new ArrayList<>(projectIdSet));
            projects = projectMapper.selectByExample(projectExample);
        }

        for (OrgUserExtend orgUserExtend : orgUserExtends) {
            if (projects.size() > 0) {
                Set<String> projectIds = userIdProjectIdMap.get(orgUserExtend.getId());
                if (CollectionUtils.isNotEmpty(projectIds)) {
                    List<Project> projectFilters = projects.stream().filter(t -> projectIds.contains(t.getId())).toList();
                    List<OptionDTO> projectList = new ArrayList<>();
                    setProjectList(projectList, projectFilters);
                    orgUserExtend.setProjectIdNameMap(projectList);
                }
            }

            Set<String> userRoleIds = userIdRoleIdMap.get(orgUserExtend.getId());
            List<UserRole> userRoleFilters = userRoles.stream().filter(t -> userRoleIds.contains(t.getId())).toList();
            List<OptionDTO> userRoleList = new ArrayList<>();
            setUserRoleList(userRoleList, userRoleFilters);
            orgUserExtend.setUserRoleIdNameMap(userRoleList);

        }
        return orgUserExtends;
    }

    private void getTargetIds(Map<String, Set<String>> userIdTargetIdMap, Set<String> targetIdSet, String sourceId, String userId) {
        Set<String> targetIds = userIdTargetIdMap.get(userId);
        if (CollectionUtils.isEmpty(targetIds)) {
            targetIds = new HashSet<>();
        }
        targetIds.add(sourceId);
        targetIdSet.add(sourceId);
        userIdTargetIdMap.put(userId, targetIds);
    }

    private void setProjectList(List<OptionDTO> projectList, List<Project> projectFilters) {
        for (Project project : projectFilters) {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(project.getId());
            optionDTO.setName(project.getName());
            projectList.add(optionDTO);
        }
    }

    public List<OrganizationProjectOptionsDTO> getOrganizationOptions() {
        return extOrganizationMapper.selectOrganizationOptions();
    }

    public void addMemberByOrg(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId) {
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        checkOrgExistById(organizationId);
        Map<String, User> userMap;
        userMap = getUserMap(organizationMemberExtendRequest);
        Map<String, UserRole> userRoleMap = checkUseRoleExist(organizationMemberExtendRequest.getUserRoleIds(), organizationId);
        setRelationByMemberAndGroupIds(organizationMemberExtendRequest, createUserId, userMap, userRoleMap, true);
    }

    private Map<String, User> getUserMap(OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        Map<String, User> userMap;
        if (organizationMemberExtendRequest.isSelectAll()) {
            OrganizationRequest organizationRequest = new OrganizationRequest();
            BeanUtils.copyBean(organizationRequest, organizationMemberExtendRequest);
            List<OrgUserExtend> orgUserExtends = extOrganizationMapper.listMemberByOrg(organizationRequest);
            List<String> excludeIds = organizationMemberExtendRequest.getExcludeIds();
            if (CollectionUtils.isNotEmpty(excludeIds)) {
                userMap = orgUserExtends.stream().filter(user -> !excludeIds.contains(user.getId())).collect(Collectors.toMap(User::getId, user -> user));
            } else {
                userMap = orgUserExtends.stream().collect(Collectors.toMap(User::getId, user -> user));
            }

        } else {
            userMap = checkUserExist(organizationMemberExtendRequest.getMemberIds());
        }
        return userMap;
    }

    private void setRelationByMemberAndGroupIds(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId, Map<String, User> userMap, Map<String, UserRole> userRoleMap, boolean add) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserRoleRelationMapper userRoleRelationMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        List<LogDTO> logDTOList = new ArrayList<>();
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        userMap.keySet().forEach(memberId -> {
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
                        UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, organizationId, userRoleId, organizationId);
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
        Map<String, User> userMap;
        userMap = getUserMap(organizationMemberExtendRequest);
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
        Map<String, User> userMap;
        if (orgMemberExtendProjectRequest.isSelectAll()) {
            OrganizationRequest organizationRequest = new OrganizationRequest();
            BeanUtils.copyBean(organizationRequest, orgMemberExtendProjectRequest);
            List<OrgUserExtend> orgUserExtends = extOrganizationMapper.listMemberByOrg(organizationRequest);
            List<String> excludeIds = orgMemberExtendProjectRequest.getExcludeIds();
            if (CollectionUtils.isNotEmpty(excludeIds)) {
                userMap = orgUserExtends.stream().filter(user -> !excludeIds.contains(user.getId())).collect(Collectors.toMap(User::getId, user -> user));
            } else {
                userMap = orgUserExtends.stream().collect(Collectors.toMap(User::getId, user -> user));
            }
        } else {
            userMap = checkUserExist(orgMemberExtendProjectRequest.getMemberIds());
        }
        List<String> userIds = userMap.values().stream().map(User::getId).toList();
        userIds.forEach(memberId -> {
            projectIds.forEach(projectId -> {
                //过滤已存在的关系
                UserRoleRelationExample example = new UserRoleRelationExample();
                example.createCriteria().andSourceIdEqualTo(projectId).andUserIdEqualTo(memberId);
                List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(userRoleRelations)) {
                    UserRoleRelation userRoleRelation = buildUserRoleRelation(userId, memberId, projectId, InternalUserRole.PROJECT_MEMBER.getValue(), requestOrganizationId);
                    userRoleRelation.setOrganizationId(orgMemberExtendProjectRequest.getOrganizationId());
                    userRoleRelationMapper.insert(userRoleRelation);
                    //add Log
                    LogDTO dto = new LogDTO(
                            projectId,
                            requestOrganizationId,
                            memberId,
                            userId,
                            OperationLogType.ADD.name(),
                            OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
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
        List<LogDTO> dtoList = new ArrayList<>();
        User user = userMapper.selectByPrimaryKey(userId);
        if (CollectionUtils.isNotEmpty(projectIds)) {
            // 项目层级日志
            example.createCriteria().andUserIdEqualTo(userId).andSourceIdIn(projectIds);
            List<UserRoleRelation> userRoleWidthProjectRelations = userRoleRelationMapper.selectByExample(example);
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
        }

        example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleWidthOrgRelations = userRoleRelationMapper.selectByExample(example);
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
    public void updateMember(OrganizationMemberUpdateRequest organizationMemberUpdateRequest, String createUserId, String path, String module) {
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
        updateUserRoleRelation(createUserId, organizationId, user, userRoleIds, sqlSession, logDTOList, path, module);
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
            UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, projectId, InternalUserRole.PROJECT_MEMBER.getValue(), organizationId);
            userRoleRelation.setOrganizationId(organizationId);
            userRoleRelationMapper.insert(userRoleRelation);
        });
    }

    private UserRoleRelation buildUserRoleRelation(String createUserId, String memberId, String sourceId, String roleId, String organizationId) {
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setId(IDGenerator.nextStr());
        userRoleRelation.setUserId(memberId);
        userRoleRelation.setOrganizationId(organizationId);
        userRoleRelation.setSourceId(sourceId);
        userRoleRelation.setRoleId(roleId);
        userRoleRelation.setCreateTime(System.currentTimeMillis());
        userRoleRelation.setCreateUser(createUserId);
        return userRoleRelation;
    }

    private void updateUserRoleRelation(String createUserId, String organizationId, User user, List<String> userRoleIds, SqlSession sqlSession, List<LogDTO> logDTOList, String path, String module) {
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
            UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, organizationId, userRoleId, organizationId);
            userRoleRelation.setOrganizationId(organizationId);
            userRoleRelationMapper.insert(userRoleRelation);
        });
        //add Log
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                organizationId,
                memberId,
                createUserId,
                OperationLogType.UPDATE.name(),
                module,
                user.getName());
        setLog(dto, path, logDTOList, userRoleInDBInOrgIds);
    }

    /**
     * 获取当前组织下的所有项目
     *
     * @param organizationId 组织ID
     * @return 项目列表
     */
    public List<OptionDTO> getProjectList(String organizationId, String keyword) {
        //校验组织是否存在
        checkOrgExistById(organizationId);
        return extSystemOrgProjectMapper.selectListProjectByOrg(organizationId, keyword);
    }

    /**
     * 获取当前组织下的所有自定义用户组以及组织级别的用户组
     *
     * @param organizationId 组织ID
     * @return 用户组列表
     */
    public List<OptionDTO> getUserRoleList(String organizationId) {
        //校验组织是否存在
        checkOrgExistById(organizationId);
        List<String> scopeIds = Arrays.asList(UserRoleEnum.GLOBAL.toString(), organizationId);
        List<OptionDTO> userRoleList = new ArrayList<>();
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andTypeEqualTo(UserRoleType.ORGANIZATION.toString()).andScopeIdIn(scopeIds);
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        setUserRoleList(userRoleList, userRoles);
        return userRoleList;
    }

    private static void setUserRoleList(List<OptionDTO> userRoleList, List<UserRole> userRoles) {
        for (UserRole userRole : userRoles) {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(userRole.getId());
            optionDTO.setName(userRole.getName());
            userRoleList.add(optionDTO);
        }
    }

    /**
     * 获取所有未被删除用户
     *
     * @param organizationId 组织ID
     * @return 用户列表
     */
    public List<OptionDisabledDTO> getUserList(String organizationId, String keyword) {
        //校验组织是否存在
        checkOrgExistById(organizationId);

        List<OptionDisabledDTO> optionDisabledDTOS = extOrganizationMapper.selectListMemberByOrg(keyword);

        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();

        for (OptionDisabledDTO optionDisabledDTO : optionDisabledDTOS) {
            if (CollectionUtils.isNotEmpty(userIds) && userIds.contains(optionDisabledDTO.getId())) {
                optionDisabledDTO.setDisabled(true);
            }
        }

        return optionDisabledDTOS;
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
    public void checkOrgExistById(String organizationId) {
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
    public Map<String, UserRole> checkUseRoleExist(List<String> userRoleIds, String organizationId) {
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
        if (CollectionUtils.isEmpty(userIds)) {
            throw new MSException(Translator.get("user.not.empty"));
        }
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
     * 设置列表其他信息(用户信息, 项目及成员数量)
     *
     * @param organizationDTOS 组织集合
     * @return 组织集合
     */
    private List<OrganizationDTO> buildExtraInfo(List<OrganizationDTO> organizationDTOS, String currentUser) {
        List<String> userIds = new ArrayList<>();
        userIds.addAll(organizationDTOS.stream().map(OrganizationDTO::getCreateUser).toList());
        userIds.addAll(organizationDTOS.stream().map(OrganizationDTO::getUpdateUser).toList());
        userIds.addAll(organizationDTOS.stream().map(OrganizationDTO::getDeleteUser).toList());
        Map<String, String> userMap = userLoginService.getUserNameMap(userIds.stream().distinct().toList());
        List<String> ids = organizationDTOS.stream().map(OrganizationDTO::getId).toList();
        List<OrganizationCountDTO> orgCountList = extOrganizationMapper.getCountByIds(ids);
        // 是否拥有组织
        boolean isSuper = baseUserMapper.isSuperUser(currentUser);
        List<String> relatedOrganizationIds = extOrganizationMapper.getRelatedOrganizationIds(currentUser);
        Map<String, OrganizationCountDTO> orgCountMap = orgCountList.stream().collect(Collectors.toMap(OrganizationCountDTO::getId, count -> count));
        organizationDTOS.forEach(organizationDTO -> {
            organizationDTO.setCreateUser(userMap.get(organizationDTO.getCreateUser()));
            organizationDTO.setDeleteUser(userMap.get(organizationDTO.getDeleteUser()));
            organizationDTO.setUpdateUser(userMap.get(organizationDTO.getUpdateUser()));
            organizationDTO.setProjectCount(orgCountMap.get(organizationDTO.getId()).getProjectCount());
            organizationDTO.setMemberCount(orgCountMap.get(organizationDTO.getId()).getMemberCount());
            if (BooleanUtils.isTrue(organizationDTO.getDeleted())) {
                organizationDTO.setRemainDayCount(getDeleteRemainDays(organizationDTO.getDeleteTime()));
            }
            organizationDTO.setSwitchAndEnter(isSuper || relatedOrganizationIds.contains(organizationDTO.getId()));
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

    /**
     * 校验组织是否存在
     * 这里使用静态方法，避免需要注入，导致循环依赖
     *
     * @param id
     * @return
     */
    public static Organization checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(CommonBeanFactory.getBean(OrganizationMapper.class).selectByPrimaryKey(id), "permission.system_organization_project.name");
    }

    /**
     * 剩余天数
     *
     * @param deleteTime 删除时间
     * @return 剩余天数
     */
    private Integer getDeleteRemainDays(Long deleteTime) {
        long remainDays = (System.currentTimeMillis() - deleteTime) / (1000 * 3600 * 24);
        int remainDayCount = DEFAULT_REMAIN_DAY_COUNT - (int) remainDays;
        return remainDayCount > 0 ? remainDayCount : 1;
    }

    /**
     * 校验组织不存在
     *
     * @param id 组织ID
     */
    private void checkOrganizationNotExist(String id) {
        if (organizationMapper.selectByPrimaryKey(id) == null) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
    }

    /**
     * 校验组织存在
     *
     * @param organizationDTO 组织DTO
     */
    private void checkOrganizationExist(OrganizationDTO organizationDTO) {
        OrganizationExample example = new OrganizationExample();
        OrganizationExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(organizationDTO.getName()).andIdNotEqualTo(organizationDTO.getId());
        if (organizationMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("organization_name_already_exists"));
        }
    }

    /**
     * 获取组织下所有管理员ID
     *
     * @param organizationId 组织ID
     * @return 管理员ID集合
     */
    public List<String> getOrgAdminIds(String organizationId) {
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(organizationId).andRoleIdEqualTo(InternalUserRole.ORG_ADMIN.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        return userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
    }

    /**
     * 成员添加组织管理员
     *
     * @param memberId       成员ID
     * @param organizationId 组织ID
     * @param createUser     创建用户
     */
    public void createAdmin(String memberId, String organizationId, String createUser) {
        UserRoleRelation orgAdmin = new UserRoleRelation();
        orgAdmin.setId(IDGenerator.nextStr());
        orgAdmin.setUserId(memberId);
        orgAdmin.setRoleId(InternalUserRole.ORG_ADMIN.getValue());
        orgAdmin.setSourceId(organizationId);
        orgAdmin.setCreateTime(System.currentTimeMillis());
        orgAdmin.setCreateUser(createUser);
        orgAdmin.setOrganizationId(organizationId);
        userRoleRelationMapper.insertSelective(orgAdmin);
    }

    /**
     * 获取当前用户所拥有的组织
     *
     * @param userId 用户ID
     * @return 组织下拉选项
     */
    public List<OptionDTO> getSwitchOption(String userId) {
        List<Organization> organizations = new ArrayList<>();
        boolean isSuper = baseUserMapper.isSuperUser(userId);
        if (isSuper) {
            // 超级管理员
            organizations = organizationMapper.selectByExample(new OrganizationExample());
        } else {
            List<String> relatedOrganizationIds = extOrganizationMapper.getRelatedOrganizationIds(userId);
            if (CollectionUtils.isNotEmpty(relatedOrganizationIds)) {
                OrganizationExample example = new OrganizationExample();
                example.createCriteria().andIdIn(relatedOrganizationIds);
                organizations = organizationMapper.selectByExample(example);
            }
        }
        return organizations.stream().map(organization -> {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(organization.getId());
            optionDTO.setName(organization.getName());
            return optionDTO;
        }).toList();
    }

    /**
     * 校验组织是否为默认组织
     *
     * @param id 组织ID
     */
    private void checkOrgDefault(String id) {
        Organization organization = organizationMapper.selectByPrimaryKey(id);
        if (organization.getNum().equals(DEFAULT_ORGANIZATION_NUM)) {
            throw new MSException(Translator.get("default_organization_not_allow_delete"));
        }
    }
}
