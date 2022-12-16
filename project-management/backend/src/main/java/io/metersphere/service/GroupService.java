package io.metersphere.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtGroupMapper;
import io.metersphere.base.mapper.ext.BaseUserGroupMapper;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.RedisKey;
import io.metersphere.commons.constants.UserGroupConstants;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.request.GroupRequest;
import io.metersphere.request.group.EditGroupRequest;
import io.metersphere.request.group.EditGroupUserRequest;
import io.metersphere.xpack.quota.service.QuotaService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.ss.formula.functions.T;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class GroupService {


    @Resource
    private BaseUserGroupMapper baseUserGroupMapper;
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private ExtGroupMapper extGroupMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserGroupPermissionMapper userGroupPermissionMapper;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private MicroService microService;
    @Resource
    private BaseUserService baseUserService;
    private static final String GLOBAL = "global";
    private static final String SUPER_GROUP = "super_group";


    // 服务权限拼装顺序
    private static final String[] servicePermissionLoadOrder = {MicroServiceName.PROJECT_MANAGEMENT,
            MicroServiceName.TEST_TRACK, MicroServiceName.API_TEST, MicroServiceName.UI_TEST,
            MicroServiceName.PERFORMANCE_TEST, MicroServiceName.REPORT_STAT, MicroServiceName.SYSTEM_SETTING};

    private static final Map<String, List<String>> map = new HashMap<>(4) {{
        put(UserGroupType.SYSTEM, Arrays.asList(UserGroupType.SYSTEM, UserGroupType.WORKSPACE, UserGroupType.PROJECT));
        put(UserGroupType.WORKSPACE, Arrays.asList(UserGroupType.WORKSPACE, UserGroupType.PROJECT));
        put(UserGroupType.PROJECT, Collections.singletonList(UserGroupType.PROJECT));
    }};

    private static final Map<String, String> typeMap = new HashMap<>(4) {{
        put(UserGroupType.SYSTEM, "系统");
        put(UserGroupType.WORKSPACE, "工作空间");
        put(UserGroupType.PROJECT, "项目");
    }};

    public Pager<List<GroupDTO>> getProjectGroupList(EditGroupRequest request) {
        SessionUser user = SessionUtils.getUser();
        List<UserGroupDTO> userGroup = baseUserGroupMapper.getUserGroup(Objects.requireNonNull(user).getId(), request.getProjectId());
        List<String> groupTypeList = userGroup.stream().map(UserGroupDTO::getType).distinct().collect(Collectors.toList());
        if (groupTypeList.isEmpty()) {
            if (baseUserService.isSuperUser(user.getId())) {
                groupTypeList.add(UserGroupType.PROJECT);
            }
        }
        return getGroups(groupTypeList, request);
    }

    public void buildUserInfo(List<GroupDTO> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        List<String> userIds = groups.stream().map(GroupDTO::getCreator).collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(userIds);
            groups.forEach(caseResult -> {
                caseResult.setCreator(userMap.getOrDefault(caseResult.getCreator(), caseResult.getCreator()));
            });
        }
    }

    public Group addGroup(EditGroupRequest request) {
        Group group = new Group();
        checkGroupExist(request);
        group.setId(request.getId());
        group.setName(request.getName());
        group.setCreator(SessionUtils.getUserId());
        group.setDescription(request.getDescription());
        group.setSystem(false);
        group.setType(request.getType());
        group.setCreateTime(System.currentTimeMillis());
        group.setUpdateTime(System.currentTimeMillis());
        if (BooleanUtils.isTrue(request.getGlobal())) {
            group.setScopeId(GLOBAL);
        } else {
            group.setScopeId(request.getScopeId());
        }
        groupMapper.insertSelective(group);
        return group;
    }

    private void checkGroupExist(EditGroupRequest request) {
        String name = request.getName();
        String id = request.getId();
        GroupExample groupExample = new GroupExample();
        GroupExample.Criteria criteria = groupExample.createCriteria();
        criteria.andNameEqualTo(name);
        if (StringUtils.isNotBlank(id)) {
            criteria.andIdNotEqualTo(id);
        }
        List<Group> groups = groupMapper.selectByExample(groupExample);
        if (CollectionUtils.isNotEmpty(groups)) {
            MSException.throwException("用户组名称已存在！");
        }
    }

    public void editGroup(EditGroupRequest request) {
        if (StringUtils.equals(request.getId(), UserGroupConstants.SUPER_GROUP)) {
            MSException.throwException("超级管理员无法编辑！");
        }
        if (StringUtils.equals(request.getId(), UserGroupConstants.ADMIN)) {
            MSException.throwException("系统管理员无法编辑！");
        }
        checkGroupExist(request);
        Group group = new Group();
        request.setScopeId(null);
        BeanUtils.copyBean(group, request);
        group.setCreator(SessionUtils.getUserId());
        group.setUpdateTime(System.currentTimeMillis());
        groupMapper.updateByPrimaryKeySelective(group);
    }

    public void deleteGroup(String id) {
        Group group = groupMapper.selectByPrimaryKey(id);
        if (group != null) {
            if (BooleanUtils.isTrue(group.getSystem())) {
                MSException.throwException("系统用户组不支持删除！");
            }
        }
        groupMapper.deleteByPrimaryKey(id);

        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andGroupIdEqualTo(id);
        userGroupMapper.deleteByExample(userGroupExample);

        UserGroupPermissionExample example = new UserGroupPermissionExample();
        example.createCriteria().andGroupIdEqualTo(id);
        userGroupPermissionMapper.deleteByExample(example);
    }

    public GroupPermissionDTO getGroupResource(Group group) {
        GroupPermissionDTO dto = new GroupPermissionDTO();
        UserGroupPermissionExample example = new UserGroupPermissionExample();
        example.createCriteria().andGroupIdEqualTo(group.getId());
        List<UserGroupPermission> groupPermissions = userGroupPermissionMapper.selectByExample(example);
        List<String> groupPermissionIds = groupPermissions.stream().map(UserGroupPermission::getPermissionId).collect(Collectors.toList());
        GroupJson groupJson = this.loadPermissionJsonFromService();
        if (groupJson == null) {
            MSException.throwException(Translator.get("read_permission_file_fail"));
        }
        List<GroupResource> resource = groupJson.getResource();
        List<GroupPermission> permissions = groupJson.getPermissions();
        List<GroupResourceDTO> dtoPermissions = dto.getPermissions();
        dtoPermissions.addAll(getResourcePermission(resource, permissions, group.getType(), groupPermissionIds));
        return dto;
    }

    private GroupJson loadPermissionJsonFromService() {
        GroupJson groupJson = null;
        try {
            for (String service : servicePermissionLoadOrder) {
                Object obj = stringRedisTemplate.opsForHash().get(RedisKey.MS_PERMISSION_KEY, service);
                if (obj == null) {
                    LogUtil.warn("permission json file is null. service name: " + service);
                    continue;
                }
                GroupJson temp = JSON.parseObject((String) obj, GroupJson.class);
                if (groupJson == null) {
                    groupJson = temp;
                } else {
                    groupJson.getResource().addAll(temp.getResource());
                    groupJson.getPermissions().addAll(temp.getPermissions());
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return groupJson;
    }

    public void editGroupPermission(EditGroupRequest request) {
        if (StringUtils.equals(request.getUserGroupId(), SUPER_GROUP)) {
            return;
        }
        List<GroupPermission> permissions = request.getPermissions();
        if (CollectionUtils.isEmpty(permissions)) {
            return;
        }

        UserGroupPermissionExample userGroupPermissionExample = new UserGroupPermissionExample();
        userGroupPermissionExample.createCriteria().andGroupIdEqualTo(request.getUserGroupId());
        userGroupPermissionMapper.deleteByExample(userGroupPermissionExample);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserGroupPermissionMapper mapper = sqlSession.getMapper(UserGroupPermissionMapper.class);
        String groupId = request.getUserGroupId();
        permissions.forEach(permission -> {
            if (BooleanUtils.isTrue(permission.getChecked())) {
                String permissionId = permission.getId();
                String resourceId = permission.getResourceId();
                UserGroupPermission groupPermission = new UserGroupPermission();
                groupPermission.setId(UUID.randomUUID().toString());
                groupPermission.setGroupId(groupId);
                groupPermission.setPermissionId(permissionId);
                groupPermission.setModuleId(resourceId);
                mapper.insert(groupPermission);
            }
        });
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public List<Group> getGroupByType(EditGroupRequest request) {
        List<Group> list = new ArrayList<>();
        GroupExample example = new GroupExample();
        GroupExample.Criteria criteria = example.createCriteria();
        String type = request.getType();
        if (StringUtils.isBlank(type)) {
            return list;
        }

        if (!StringUtils.equals(type, UserGroupType.SYSTEM)) {
            criteria.andTypeEqualTo(type);
        }

        if (BooleanUtils.isTrue(request.isOnlyQueryGlobal())) {
            criteria.andScopeIdEqualTo(GLOBAL);
        }

        return groupMapper.selectByExample(example);
    }

    public List<Map<String, Object>> getAllUserGroup(String userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> groupsIds = userGroups.stream().map(UserGroup::getGroupId).distinct().collect(Collectors.toList());
        for (String id : groupsIds) {
            Group group = groupMapper.selectByPrimaryKey(id);
            String type = group.getType();
            Map<String, Object> map = new HashMap<>(2);
            map.put("type", id + "+" + type);
            WorkspaceResource workspaceResource = listResource(id, group.getType());
            List<String> collect = userGroups.stream().filter(ugp -> ugp.getGroupId().equals(id)).map(UserGroup::getSourceId).collect(Collectors.toList());
            map.put("ids", collect);
            if (StringUtils.equals(type, UserGroupType.WORKSPACE)) {
                map.put("workspaces", workspaceResource.getWorkspaces());
            }
            if (StringUtils.equals(type, UserGroupType.PROJECT)) {
                map.put("projects", workspaceResource.getProjects());
            }
            list.add(map);
        }
        return list;
    }

    public WorkspaceResource listResource(String groupId, String type) {
        Group group = groupMapper.selectByPrimaryKey(groupId);
        String workspaceId = group.getScopeId();
        WorkspaceResource resource = new WorkspaceResource();

        if (StringUtils.equals(UserGroupType.WORKSPACE, type)) {
            WorkspaceExample workspaceExample = new WorkspaceExample();
            WorkspaceExample.Criteria criteria = workspaceExample.createCriteria();
            if (!StringUtils.equals(workspaceId, "global")) {
                criteria.andIdEqualTo(workspaceId);
            }
            List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
            resource.setWorkspaces(workspaces);
        }

        if (StringUtils.equals(UserGroupType.PROJECT, type)) {
            ProjectExample projectExample = new ProjectExample();
            ProjectExample.Criteria pc = projectExample.createCriteria();
            WorkspaceExample workspaceExample = new WorkspaceExample();
            WorkspaceExample.Criteria criteria = workspaceExample.createCriteria();
            if (!StringUtils.equals(workspaceId, "global")) {
                criteria.andIdEqualTo(workspaceId);
                List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
                List<String> list = workspaces.stream().map(Workspace::getId).collect(Collectors.toList());
                pc.andWorkspaceIdIn(list);
            }
            List<Project> projects = projectMapper.selectByExample(projectExample);
            resource.setProjects(projects);
        }

        return resource;
    }

    public List<Group> getGroupsByType(GroupRequest request) {
        String resourceId = request.getResourceId();
        String type = request.getType();
        List<String> scopeList = Arrays.asList(GLOBAL, resourceId);
        if (StringUtils.equals(type, UserGroupType.PROJECT) && StringUtils.isNotBlank(request.getProjectId())) {
            scopeList = Arrays.asList(GLOBAL, resourceId, request.getProjectId());
        }
        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andScopeIdIn(scopeList)
                .andTypeEqualTo(type);
        return groupMapper.selectByExample(groupExample);
    }

    public List<Group> getWorkspaceMemberGroups(String workspaceId, String userId) {
        return baseUserGroupMapper.getWorkspaceMemberGroups(workspaceId, userId);
    }

    private List<GroupResourceDTO> getResourcePermission(List<GroupResource> resource, List<GroupPermission> permissions, String type, List<String> permissionList) {
        List<GroupResourceDTO> dto = new ArrayList<>();
        List<GroupResource> resources = resource.stream().filter(g -> g.getId().startsWith(type) || g.getId().startsWith("PERSONAL")).collect(Collectors.toList());
        permissions.forEach(p -> {
            if (permissionList.contains(p.getId())) {
                p.setChecked(true);
            }
        });
        for (GroupResource r : resources) {
            GroupResourceDTO resourceDTO = new GroupResourceDTO();
            resourceDTO.setResource(r);
            List<GroupPermission> collect = permissions
                    .stream()
                    .filter(p -> StringUtils.equals(r.getId(), p.getResourceId()))
                    .collect(Collectors.toList());
            resourceDTO.setPermissions(collect);
            resourceDTO.setType(r.getId().split("_")[0]);
            dto.add(resourceDTO);
        }
        return dto;
    }

    private Pager<List<GroupDTO>> getGroups(List<String> groupTypeList, EditGroupRequest request) {
        if (groupTypeList.contains(UserGroupType.SYSTEM)) {
            return getUserGroup(UserGroupType.SYSTEM, request);
        }

        if (groupTypeList.contains(UserGroupType.WORKSPACE)) {
            return getUserGroup(UserGroupType.WORKSPACE, request);
        }

        if (groupTypeList.contains(UserGroupType.PROJECT)) {
            return getUserGroup(UserGroupType.PROJECT, request);
        }

        return new Pager<>();
    }

    private Pager<List<GroupDTO>> getUserGroup(String groupType, EditGroupRequest request) {
        List<String> types;
        String workspaceId = SessionUtils.getCurrentWorkspaceId();
        String projectId = SessionUtils.getCurrentProjectId();
        List<String> scopes = Arrays.asList(GLOBAL, workspaceId, projectId);
        int goPage = request.getGoPage();
        int pageSize = request.getPageSize();
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        if (StringUtils.equals(groupType, UserGroupType.SYSTEM)) {
            scopes = new ArrayList<>();
        }
        types = map.get(groupType);
        request.setTypes(types);
        request.setScopes(scopes);
        List<GroupDTO> groups = extGroupMapper.getGroupList(request);
        buildUserInfo(groups);
        return PageUtils.setPageInfo(page, groups);
    }

    public List<Group> getProjectMemberGroups(String projectId, String userId) {
        return baseUserGroupMapper.getProjectMemberGroups(projectId, userId);
    }

    public List<GroupDTO> getAllGroup() {
        List<String> types = map.get(UserGroupType.SYSTEM);
        EditGroupRequest request = new EditGroupRequest();
        request.setTypes(types);
        return extGroupMapper.getGroupList(request);
    }

    public List<?> getResource(String type, String groupId) {
        List<T> resource = new ArrayList<>();
        Group group = groupMapper.selectByPrimaryKey(groupId);
        if (group == null) {
            return resource;
        }

        String scopeId = group.getScopeId();
        if (StringUtils.equals(UserGroupType.WORKSPACE, type)) {
            WorkspaceExample workspaceExample = new WorkspaceExample();
            WorkspaceExample.Criteria criteria = workspaceExample.createCriteria();
            if (!StringUtils.equals(scopeId, GLOBAL)) {
                criteria.andIdEqualTo(scopeId);
            }
            return workspaceMapper.selectByExample(workspaceExample);
        }

        if (StringUtils.equals(UserGroupType.PROJECT, type)) {
            ProjectExample projectExample = new ProjectExample();
            ProjectExample.Criteria criteria = projectExample.createCriteria();
            if (StringUtils.equals(scopeId, GLOBAL)) {
                return projectMapper.selectByExample(projectExample);
            }
            Workspace workspace = workspaceMapper.selectByPrimaryKey(scopeId);
            if (workspace != null) {
                criteria.andWorkspaceIdEqualTo(workspace.getId());
                return projectMapper.selectByExample(projectExample);
            }
            Project project = projectMapper.selectByPrimaryKey(scopeId);
            List<Project> list = new ArrayList<>();
            if (project != null) {
                list.add(project);
            }
            return list;
        }

        return resource;
    }

    public List<User> getGroupUser(EditGroupRequest request) {
        return baseUserGroupMapper.getGroupUser(request);
    }

    public void removeGroupMember(String userId, String groupId) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria()
                .andGroupIdEqualTo(groupId)
                .andUserIdEqualTo(userId);
        userGroupMapper.deleteByExample(userGroupExample);
    }

    public List<?> getGroupSource(String userId, String groupId) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId).andGroupIdEqualTo(groupId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> sources = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());
        if (sources.isEmpty()) {
            return new ArrayList<>();
        }

        Group group = groupMapper.selectByPrimaryKey(groupId);
        String type = group.getType();

        if (StringUtils.equals(type, UserGroupType.WORKSPACE)) {
            WorkspaceExample workspaceExample = new WorkspaceExample();
            workspaceExample.createCriteria().andIdIn(sources);
            return workspaceMapper.selectByExample(workspaceExample);
        }

        if (StringUtils.equals(type, UserGroupType.PROJECT)) {
            ProjectExample projectExample = new ProjectExample();
            projectExample.createCriteria().andIdIn(sources);
            return projectMapper.selectByExample(projectExample);
        }

        return new ArrayList<>();
    }

    public void addGroupUser(EditGroupUserRequest request) {
        if (StringUtils.isBlank(request.getGroupId()) || CollectionUtils.isEmpty(request.getUserIds())) {
            LogUtil.info("add group user warning, please check param!");
            return;
        }

        Group group = groupMapper.selectByPrimaryKey(request.getGroupId());
        if (group == null) {
            LogUtil.info("add group user warning, group is null. group id: " + request.getGroupId());
            return;
        }

        if (StringUtils.equals(group.getType(), UserGroupType.SYSTEM)) {
            this.addSystemGroupUser(group, request.getUserIds());
        } else {
            if (CollectionUtils.isNotEmpty(request.getSourceIds())) {
                this.addNotSystemGroupUser(group, request.getUserIds(), request.getSourceIds());
            }
        }
    }

    private void addSystemGroupUser(Group group, List<String> userIds) {
        for (String userId : userIds) {
            User user = userMapper.selectByPrimaryKey(userId);
            if (user == null) {
                continue;
            }
            UserGroupExample userGroupExample = new UserGroupExample();
            userGroupExample.createCriteria().andUserIdEqualTo(userId).andGroupIdEqualTo(group.getId());
            List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
            if (userGroups.size() <= 0) {
                UserGroup userGroup = new UserGroup(UUID.randomUUID().toString(), userId, group.getId(),
                        "system", System.currentTimeMillis(), System.currentTimeMillis());
                userGroupMapper.insertSelective(userGroup);
            }
        }
    }

    private void addNotSystemGroupUser(Group group, List<String> userIds, List<String> sourceIds) {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        for (String userId : userIds) {
            User user = userMapper.selectByPrimaryKey(userId);
            if (user == null) {
                continue;
            }
            checkQuota(quotaService, group.getType(), sourceIds, Collections.singletonList(userId));
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            UserGroupMapper mapper = sqlSession.getMapper(UserGroupMapper.class);
            UserGroupExample userGroupExample = new UserGroupExample();
            userGroupExample.createCriteria().andUserIdEqualTo(userId).andGroupIdEqualTo(group.getId());
            List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
            List<String> existSourceIds = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());
            List<String> toAddSourceIds = new ArrayList<>(sourceIds);
            toAddSourceIds.removeAll(existSourceIds);

            for (String sourceId : toAddSourceIds) {
                UserGroup userGroup = new UserGroup(UUID.randomUUID().toString(), userId, group.getId(),
                        sourceId, System.currentTimeMillis(), System.currentTimeMillis());
                mapper.insertSelective(userGroup);
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }


    private void checkQuota(QuotaService quotaService, String type, List<String> sourceIds, List<String> userIds) {
        if (quotaService != null) {
            Map<String, List<String>> addMemberMap = sourceIds.stream().collect(Collectors.toMap( id -> id, id -> userIds));
            quotaService.checkMemberCount(addMemberMap, type);
        }
    }

    public void editGroupUser(EditGroupUserRequest request) {
        String groupId = request.getGroupId();
        Group group = groupMapper.selectByPrimaryKey(groupId);
        if (!StringUtils.equals(group.getType(), UserGroupType.SYSTEM)) {
            List<String> userIds = request.getUserIds();
            if (CollectionUtils.isNotEmpty(userIds)) {
                // 编辑单个用户
                String userId = userIds.get(0);
                List<String> sourceIds = request.getSourceIds();
                UserGroupExample userGroupExample = new UserGroupExample();
                userGroupExample.createCriteria().andGroupIdEqualTo(groupId).andUserIdEqualTo(userId);
                userGroupMapper.deleteByExample(userGroupExample);

                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                UserGroupMapper mapper = sqlSession.getMapper(UserGroupMapper.class);
                for (String sourceId : sourceIds) {
                    UserGroup userGroup = new UserGroup();
                    userGroup.setId(UUID.randomUUID().toString());
                    userGroup.setUserId(userId);
                    userGroup.setGroupId(groupId);
                    userGroup.setSourceId(sourceId);
                    userGroup.setCreateTime(System.currentTimeMillis());
                    userGroup.setUpdateTime(System.currentTimeMillis());
                    mapper.insertSelective(userGroup);
                }
                sqlSession.flushStatements();
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
    }

    public String getLogDetails(String id) {
        Group group = groupMapper.selectByPrimaryKey(id);
        if (group != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(group, SystemReference.groupColumns);
            for (DetailColumn column : columns) {
                if ("scopeId".equals(column.getColumnName()) && column.getOriginalValue() != null && StringUtils.isNotEmpty(column.getOriginalValue().toString())) {
                    if ("global".equals(column.getOriginalValue())) {
                        column.setOriginalValue("是");
                    } else {
                        String scopeId = group.getScopeId();
                        Workspace workspace = workspaceMapper.selectByPrimaryKey(scopeId);
                        if (workspace != null) {
                            column.setOriginalValue("否; 所属工作空间：" + workspace.getName());
                        } else {
                            column.setOriginalValue("否");
                        }
                    }
                }
                if ("type".equals(column.getColumnName()) && column.getOriginalValue() != null && StringUtils.isNotEmpty(column.getOriginalValue().toString())) {
                    column.setOriginalValue(typeMap.get((String) column.getOriginalValue()));
                }
            }
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(group.getId()), null, group.getName(), group.getCreator(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public List<Workspace> getWorkspace(String userId) {
        List<Workspace> list = new ArrayList<>();
        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andTypeEqualTo(UserGroupType.WORKSPACE);
        List<Group> groups = groupMapper.selectByExample(groupExample);
        List<String> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(groups)) {
            return list;
        }
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId).andGroupIdIn(groupIds);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> workspaceIds = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(workspaceIds)) {
            return list;
        }

        WorkspaceExample workspaceExample = new WorkspaceExample();
        workspaceExample.createCriteria().andIdIn(workspaceIds);
        list = workspaceMapper.selectByExample(workspaceExample);
        return list;
    }

    public Object getWorkspaceResourceByGroup(String groupId, String groupType) {
        return microService.getForData(MicroServiceName.SYSTEM_SETTING, String.format("/workspace/list/resource/%s/%s", groupId, groupType));
    }
}
