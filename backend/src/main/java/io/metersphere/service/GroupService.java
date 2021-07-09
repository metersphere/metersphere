package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtGroupMapper;
import io.metersphere.base.mapper.ext.ExtUserGroupMapper;
import io.metersphere.commons.constants.UserGroupConstants;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.GroupRequest;
import io.metersphere.controller.request.group.EditGroupRequest;
import io.metersphere.controller.request.group.EditGroupUserRequest;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class GroupService {


    @Resource
    private ExtUserGroupMapper extUserGroupMapper;
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
    private OrganizationService organizationService;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private ProjectMapper projectMapper;

    private static final String GLOBAL = "global";

    private static final Map<String, List<String>> map = new HashMap<String, List<String>>(4){{
        put(UserGroupType.SYSTEM, Arrays.asList(UserGroupType.SYSTEM, UserGroupType.ORGANIZATION, UserGroupType.WORKSPACE, UserGroupType.PROJECT));
        put(UserGroupType.ORGANIZATION, Arrays.asList(UserGroupType.ORGANIZATION, UserGroupType.WORKSPACE, UserGroupType.PROJECT));
        put(UserGroupType.WORKSPACE, Arrays.asList(UserGroupType.WORKSPACE, UserGroupType.PROJECT));
        put(UserGroupType.PROJECT, Collections.singletonList(UserGroupType.PROJECT));
    }};

    public Pager<List<GroupDTO>> getGroupList(EditGroupRequest request) {
        SessionUser user = SessionUtils.getUser();
        List<UserGroupDTO> userGroup = extUserGroupMapper.getUserGroup(Objects.requireNonNull(user).getId());
        List<String> groupTypeList = userGroup.stream().map(UserGroupDTO::getType).collect(Collectors.toList());
        return getGroups(groupTypeList, request);
    }

    public Group addGroup(EditGroupRequest request) {
        Group group = new Group();
        checkGroupExist(request);
        group.setId(UUID.randomUUID().toString());
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
        if (StringUtils.equals(request.getId(), UserGroupConstants.ADMIN)) {
            MSException.throwException("系统管理员无法编辑！");
        }
        checkGroupExist(request);
        Group group = new Group();
        request.setScopeId(null);
        BeanUtils.copyBean(group, request);
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

    public GroupPermissionDTO getGroupResource(Group g) {
        GroupPermissionDTO dto = new GroupPermissionDTO();
        InputStream permission = getClass().getResourceAsStream("/permission.json");
        String type = g.getType();
        String id = g.getId();
        UserGroupPermissionExample userGroupPermissionExample = new UserGroupPermissionExample();
        userGroupPermissionExample.createCriteria().andGroupIdEqualTo(id);
        List<UserGroupPermission> userGroupPermissions = userGroupPermissionMapper.selectByExample(userGroupPermissionExample);
        List<String> permissionList = userGroupPermissions.stream().map(UserGroupPermission::getPermissionId).collect(Collectors.toList());
        if (permission == null) {
            throw new RuntimeException("读取文件失败!");
        } else {
            GroupJson group;
            try {
                group = JSON.parseObject(permission, GroupJson.class);
                List<GroupResource> resource = group.getResource();
                List<GroupPermission> permissions = group.getPermissions();
                List<GroupResourceDTO> dtoPermissions = dto.getPermissions();
                dtoPermissions.addAll(getResourcePermission(resource, permissions, type, permissionList));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dto;
    }

    public void editGroupPermission(EditGroupRequest request) {
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
            OrganizationResource organizationResource = organizationService.listResource(id, group.getType());
            List<String> collect = userGroups.stream().filter(ugp -> ugp.getGroupId().equals(id)).map(UserGroup::getSourceId).collect(Collectors.toList());
            map.put("ids", collect);
            if (StringUtils.equals(type, UserGroupType.ORGANIZATION)) {
                map.put("organizations", organizationResource.getOrganizations());
            }
            if (StringUtils.equals(type, UserGroupType.WORKSPACE)) {
                map.put("workspaces", organizationResource.getWorkspaces());
            }
            if (StringUtils.equals(type, UserGroupType.PROJECT)) {
                map.put("projects", organizationResource.getProjects());
            }
            list.add(map);
        }
        return list;
    }

    public List<Group> getGroupsByType(GroupRequest request) {
        String resourceId = request.getResourceId();
        String type = request.getType();
        List<String> scopeList = Arrays.asList(GLOBAL, resourceId);
        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andScopeIdIn(scopeList)
                .andTypeEqualTo(type);
        return groupMapper.selectByExample(groupExample);
    }

    public List<Group> getOrganizationMemberGroups(String orgId, String userId) {
        return extUserGroupMapper.getOrganizationMemberGroups(orgId, userId);
    }

    public List<Group> getWorkspaceMemberGroups(String workspaceId, String userId) {
        return extUserGroupMapper.getWorkspaceMemberGroups(workspaceId, userId);
    }




    private List<GroupResourceDTO> getResourcePermission(List<GroupResource> resource, List<GroupPermission> permissions, String type, List<String> permissionList) {
        List<GroupResourceDTO> dto = new ArrayList<>();
        List<GroupResource> resources = resource.stream().filter(g -> g.getId().startsWith(type)).collect(Collectors.toList());
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

        if (groupTypeList.contains(UserGroupType.ORGANIZATION)) {
            return getUserGroup(UserGroupType.ORGANIZATION, request);
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
        String orgId = SessionUtils.getCurrentOrganizationId();
        List<String> scopes = Arrays.asList(GLOBAL, orgId);
        int goPage = request.getGoPage();
        int pageSize = request.getPageSize();
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        if (StringUtils.equals(groupType, UserGroupType.SYSTEM)) {
            scopes = new ArrayList<>();
        }
        types = map.get(groupType);
        request.setTypes(types);
        request.setScopes(scopes);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<GroupDTO> groups = extGroupMapper.getGroupList(request);
        return PageUtils.setPageInfo(page, groups);
    }

    public List<Organization> getOrganization(String userId) {
        List<Organization> list = new ArrayList<>();
        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andTypeEqualTo(UserGroupType.ORGANIZATION);
        List<Group> groups = groupMapper.selectByExample(groupExample);
        List<String> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(groups)) {
            return list;
        }
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId).andGroupIdIn(groupIds);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<String> orgIds = userGroups.stream().map(UserGroup::getSourceId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(orgIds)) {
            return list;
        }

        OrganizationExample organizationExample = new OrganizationExample();
        organizationExample.createCriteria().andIdIn(orgIds);
        list = organizationMapper.selectByExample(organizationExample);
        return list;
    }

    public List<Group> getProjectMemberGroups(String projectId, String userId) {
        return extUserGroupMapper.getProjectMemberGroups(projectId, userId);
    }

    public List<Group> getAllGroup() {
        return groupMapper.selectByExample(new GroupExample());
    }

    public List<?> getResource(String type, String groupId) {
        List<T> resource = new ArrayList<>();
        Group group = groupMapper.selectByPrimaryKey(groupId);
        String orgId = group.getScopeId();
        if (!StringUtils.equals(GLOBAL, orgId)) {
            Organization organization = organizationMapper.selectByPrimaryKey(orgId);
            if (organization == null) {
                return resource;
            }
        }

        if (StringUtils.equals(UserGroupType.ORGANIZATION, type)) {
            OrganizationExample organizationExample = new OrganizationExample();
            OrganizationExample.Criteria criteria = organizationExample.createCriteria();
            if (!StringUtils.equals(orgId, GLOBAL)) {
                criteria.andIdEqualTo(orgId);
            }
           return organizationMapper.selectByExample(organizationExample);
        }

        if (StringUtils.equals(UserGroupType.WORKSPACE, type)) {
            WorkspaceExample workspaceExample = new WorkspaceExample();
            WorkspaceExample.Criteria criteria = workspaceExample.createCriteria();
            if (!StringUtils.equals(orgId, GLOBAL)) {
                criteria.andOrganizationIdEqualTo(orgId);
            }
            return workspaceMapper.selectByExample(workspaceExample);
        }

        if (StringUtils.equals(UserGroupType.PROJECT, type)) {
            ProjectExample projectExample = new ProjectExample();
            ProjectExample.Criteria pc = projectExample.createCriteria();
            WorkspaceExample workspaceExample = new WorkspaceExample();
            WorkspaceExample.Criteria criteria = workspaceExample.createCriteria();
            if (!StringUtils.equals(orgId, GLOBAL)) {
                criteria.andOrganizationIdEqualTo(orgId);
                List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
                List<String> list = workspaces.stream().map(Workspace::getId).collect(Collectors.toList());
                pc.andWorkspaceIdIn(list);
            }
            return projectMapper.selectByExample(projectExample);
        }

        return resource;
    }

    public List<User> getGroupUser(EditGroupRequest request) {
        return extUserGroupMapper.getGroupUser(request);
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

        if (StringUtils.equals(type, UserGroupType.ORGANIZATION)) {
            OrganizationExample organizationExample = new OrganizationExample();
            organizationExample.createCriteria().andIdIn(sources);
            return organizationMapper.selectByExample(organizationExample);
        }

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
        String groupId = request.getGroupId();
        Group group = groupMapper.selectByPrimaryKey(groupId);
        List<String> userIds = request.getUserIds();
        for (String userId : userIds) {
            UserGroupExample userGroupExample = new UserGroupExample();
            userGroupExample.createCriteria().andUserIdEqualTo(userId)
                    .andGroupIdEqualTo(groupId);
            List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
            if (userGroups.size() > 0) {
                MSException.throwException(Translator.get("user_already_exists") + ": " + userId);
            } else {
                this.addGroupUser(group, userId, request.getSourceIds());
            }
        }
    }

    private void addGroupUser(Group group, String userId, List<String> sourceIds) {
        String id = group.getId();
        String type = group.getType();
        if (StringUtils.equals(type, UserGroupType.SYSTEM)) {
            UserGroup userGroup = new UserGroup();
            userGroup.setId(UUID.randomUUID().toString());
            userGroup.setUserId(userId);
            userGroup.setGroupId(id);
            userGroup.setSourceId("system");
            userGroup.setCreateTime(System.currentTimeMillis());
            userGroup.setUpdateTime(System.currentTimeMillis());
            userGroupMapper.insertSelective(userGroup);
        } else {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            UserGroupMapper mapper = sqlSession.getMapper(UserGroupMapper.class);
            for (String sourceId : sourceIds) {
                UserGroup userGroup = new UserGroup();
                userGroup.setId(UUID.randomUUID().toString());
                userGroup.setUserId(userId);
                userGroup.setGroupId(id);
                userGroup.setSourceId(sourceId);
                userGroup.setCreateTime(System.currentTimeMillis());
                userGroup.setUpdateTime(System.currentTimeMillis());
                mapper.insertSelective(userGroup);
            }
            sqlSession.flushStatements();
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
            }
        }
    }
}
