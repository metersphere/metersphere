package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtOrganizationMapper;
import io.metersphere.base.mapper.ext.ExtUserGroupMapper;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import io.metersphere.commons.constants.UserGroupConstants;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.OrganizationRequest;
import io.metersphere.dto.OrganizationMemberDTO;
import io.metersphere.dto.OrganizationResource;
import io.metersphere.dto.RelatedSource;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationService {

    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private UserService userService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private ExtUserGroupMapper extUserGroupMapper;
    @Resource
    private UserGroupMapper userGroupMapper;

    public Organization addOrganization(Organization organization) {
        checkOrganization(organization);
        long currentTimeMillis = System.currentTimeMillis();
        organization.setId(organization.getId());
        organization.setCreateTime(currentTimeMillis);
        organization.setUpdateTime(currentTimeMillis);
        organization.setCreateUser(SessionUtils.getUserId());
        organizationMapper.insertSelective(organization);

        // 创建组织为当前用户添加用户组
        UserGroup userGroup = new UserGroup();
        userGroup.setId(UUID.randomUUID().toString());
        userGroup.setUserId(SessionUtils.getUserId());
        userGroup.setCreateTime(System.currentTimeMillis());
        userGroup.setUpdateTime(System.currentTimeMillis());
        userGroup.setGroupId(UserGroupConstants.ORG_ADMIN);
        userGroup.setSourceId(organization.getId());
        userGroupMapper.insert(userGroup);
        return organization;
    }

    public List<Organization> getOrganizationList(OrganizationRequest request) {
        OrganizationExample example = new OrganizationExample();
        OrganizationExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        example.setOrderByClause("update_time desc");
        return organizationMapper.selectByExample(example);
    }

    private void checkOrganization(Organization organization) {
        if (StringUtils.isBlank(organization.getName())) {
            MSException.throwException(Translator.get("organization_name_is_null"));
        }

        OrganizationExample example = new OrganizationExample();
        OrganizationExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(organization.getName());
        if (StringUtils.isNotBlank(organization.getId())) {
            criteria.andIdNotEqualTo(organization.getId());
        }

        if (organizationMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("organization_name_already_exists"));
        }

    }

    public void deleteOrganization(String organizationId) {
        WorkspaceExample example = new WorkspaceExample();
        WorkspaceExample.Criteria criteria = example.createCriteria();
        criteria.andOrganizationIdEqualTo(organizationId);

        // delete workspace
        List<Workspace> workspaces = workspaceMapper.selectByExample(example);
        List<String> workspaceIdList = workspaces.stream().map(Workspace::getId).collect(Collectors.toList());
        for (String workspaceId : workspaceIdList) {
            workspaceService.deleteWorkspace(workspaceId);
        }

        // delete user group
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andSourceIdEqualTo(organizationId);
        userGroupMapper.deleteByExample(userGroupExample);

        // delete org
        organizationMapper.deleteByPrimaryKey(organizationId);
    }

    public void updateOrganization(Organization organization) {
        checkOrganization(organization);
        organization.setCreateTime(null);
        organization.setUpdateTime(System.currentTimeMillis());
        organizationMapper.updateByPrimaryKeySelective(organization);
    }

    public List<Organization> getOrganizationListByUserId(String userId) {
        List<RelatedSource> relatedSource = extUserGroupMapper.getRelatedSource(userId);
        List<String> organizationIds = relatedSource
                .stream()
                .map(RelatedSource::getOrganizationId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(organizationIds)) {
            return new ArrayList<>();
        }
        OrganizationExample organizationExample = new OrganizationExample();
        organizationExample.createCriteria().andIdIn(organizationIds);
        return organizationMapper.selectByExample(organizationExample);
    }

    public void updateOrgMember(OrganizationMemberDTO memberDTO) {
        String orgId = memberDTO.getOrganizationId();
        String userId = memberDTO.getId();
        List<Group> memberGroups = extUserGroupMapper.getOrganizationMemberGroups(orgId, userId);
        List<String> groups = memberDTO.getGroupIds();
        List<String> allGroupIds = memberGroups.stream().map(Group::getId).collect(Collectors.toList());
        for (int i = 0; i < groups.size(); i++) {
            if (checkSourceRole(orgId, userId, groups.get(i)) == 0) {
                UserGroup userGroup = new UserGroup();
                userGroup.setId(UUID.randomUUID().toString());
                userGroup.setUserId(userId);
                userGroup.setGroupId(groups.get(i));
                userGroup.setSourceId(orgId);
                userGroup.setCreateTime(System.currentTimeMillis());
                userGroup.setUpdateTime(System.currentTimeMillis());
                userGroupMapper.insertSelective(userGroup);
            }
        }
        allGroupIds.removeAll(groups);
        if (allGroupIds.size() > 0) {
            UserGroupExample userGroupExample = new UserGroupExample();
            userGroupExample.createCriteria().andUserIdEqualTo(userId)
                    .andSourceIdEqualTo(orgId)
                    .andGroupIdIn(allGroupIds);
            userGroupMapper.deleteByExample(userGroupExample);
        }
    }

    public Integer checkSourceRole(String orgId, String userId, String groupId) {
        return extOrganizationMapper.checkSourceRole(orgId, userId, groupId);
    }

    public void checkOrgOwner(String organizationId) {
        SessionUser sessionUser = SessionUtils.getUser();
        UserDTO user = userService.getUserDTO(sessionUser.getId());
        List<String> groupIds = user.getGroups()
                .stream()
                .filter(g -> StringUtils.equals(g.getType(), UserGroupType.ORGANIZATION))
                .map(Group::getId)
                .collect(Collectors.toList());
        List<String> collect = user.getUserGroups().stream()
                .filter(ur -> groupIds.contains(ur.getGroupId()))
                .map(UserGroup::getSourceId)
                .collect(Collectors.toList());
        if (!collect.contains(organizationId)) {
            MSException.throwException(Translator.get("organization_does_not_belong_to_user"));
        }
    }

    public List<OrganizationMemberDTO> findIdAndNameByOrganizationId(String OrganizationID) {
        return extOrganizationMapper.findIdAndNameByOrganizationId(OrganizationID);
    }

    public String getLogDetails(String id) {
        Organization user = organizationMapper.selectByPrimaryKey(id);
        if (user != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(user, SystemReference.organizationColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(user.getId()), null, user.getName(), user.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public OrganizationResource listResource(String groupId, String type) {
        Group group = groupMapper.selectByPrimaryKey(groupId);
        String orgId = group.getScopeId();
        OrganizationResource resource = new OrganizationResource();
        if (!StringUtils.equals("global", orgId)) {
            Organization organization = organizationMapper.selectByPrimaryKey(orgId);
            if (organization == null) {
                return resource;
            }
        }

        if (StringUtils.equals(UserGroupType.ORGANIZATION, type)) {
            OrganizationExample organizationExample = new OrganizationExample();
            OrganizationExample.Criteria criteria = organizationExample.createCriteria();
            if (!StringUtils.equals(orgId, "global")) {
                criteria.andIdEqualTo(orgId);
            }
            List<Organization> organizations = organizationMapper.selectByExample(organizationExample);
            resource.setOrganizations(organizations);
        }

        if (StringUtils.equals(UserGroupType.WORKSPACE, type)) {
            WorkspaceExample workspaceExample = new WorkspaceExample();
            WorkspaceExample.Criteria criteria = workspaceExample.createCriteria();
            if (!StringUtils.equals(orgId, "global")) {
                criteria.andOrganizationIdEqualTo(orgId);
            }
            List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
            resource.setWorkspaces(workspaces);
        }

        if (StringUtils.equals(UserGroupType.PROJECT, type)) {
            ProjectExample projectExample = new ProjectExample();
            ProjectExample.Criteria pc = projectExample.createCriteria();
            WorkspaceExample workspaceExample = new WorkspaceExample();
            WorkspaceExample.Criteria criteria = workspaceExample.createCriteria();
            if (!StringUtils.equals(orgId, "global")) {
                criteria.andOrganizationIdEqualTo(orgId);
                List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
                List<String> list = workspaces.stream().map(Workspace::getId).collect(Collectors.toList());
                pc.andWorkspaceIdIn(list);
            }
            List<Project> projects = projectMapper.selectByExample(projectExample);
            resource.setProjects(projects);
        }

        return resource;
    }

    public String getLogDetails(OrganizationMemberDTO memberDTO) {
        String orgId = memberDTO.getOrganizationId();
        String userId = memberDTO.getId();
        // 已有角色
        List<Group> memberGroups = extUserGroupMapper.getOrganizationMemberGroups(orgId, userId);
        Organization user = organizationMapper.selectByPrimaryKey(orgId);
        if (user != null) {
            List<String> names = memberGroups.stream().map(Group::getName).collect(Collectors.toList());
            List<String> ids = memberGroups.stream().map(Group::getId).collect(Collectors.toList());
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(user, SystemReference.organizationColumns);
            DetailColumn column = new DetailColumn("成员角色", "userRoles", String.join(",", names), null);
            columns.add(column);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), null, "用户 " + userId + " 修改角色为：" + String.join(",", names), user.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
