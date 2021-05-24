package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtOrganizationMapper;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.OrganizationRequest;
import io.metersphere.dto.OrganizationMemberDTO;
import io.metersphere.dto.OrganizationResource;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserRoleHelpDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
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

    public Organization addOrganization(Organization organization) {
        checkOrganization(organization);
        long currentTimeMillis = System.currentTimeMillis();
        organization.setId(organization.getId());
        organization.setCreateTime(currentTimeMillis);
        organization.setUpdateTime(currentTimeMillis);
        organization.setCreateUser(SessionUtils.getUserId());
        organizationMapper.insertSelective(organization);
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

        // delete organization member
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andSourceIdEqualTo(organizationId);
        userRoleMapper.deleteByExample(userRoleExample);

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
        List<UserRoleHelpDTO> userRoleHelpList = extUserRoleMapper.getUserRoleHelpList(userId);
        List<String> list = new ArrayList<>();
        userRoleHelpList.forEach(r -> {
            if (StringUtils.isEmpty(r.getParentId())) {
                list.add(r.getSourceId());
            } else {
                list.add(r.getParentId());
            }
        });

        // ignore list size is 0
        list.add("no_such_id");

        OrganizationExample organizationExample = new OrganizationExample();
        organizationExample.createCriteria().andIdIn(list);
        return organizationMapper.selectByExample(organizationExample);
    }

    public void updateOrgMember(OrganizationMemberDTO memberDTO) {
        String orgId = memberDTO.getOrganizationId();
        String userId = memberDTO.getId();
        // 已有角色
        List<Role> memberRoles = extUserRoleMapper.getOrganizationMemberRoles(orgId, userId);
        // 修改后的角色
        List<String> roles = memberDTO.getRoleIds();
        List<String> allRoleIds = memberRoles.stream().map(Role::getId).collect(Collectors.toList());
        // 更新用户时添加了角色
        for (int i = 0; i < roles.size(); i++) {
            if (checkSourceRole(orgId, userId, roles.get(i)) == 0) {
                UserRole userRole = new UserRole();
                userRole.setId(UUID.randomUUID().toString());
                userRole.setUserId(userId);
                userRole.setRoleId(roles.get(i));
                userRole.setSourceId(orgId);
                userRole.setCreateTime(System.currentTimeMillis());
                userRole.setUpdateTime(System.currentTimeMillis());
                userRoleMapper.insertSelective(userRole);
            }
        }
        allRoleIds.removeAll(roles);
        if (allRoleIds.size() > 0) {
            UserRoleExample userRoleExample = new UserRoleExample();
            userRoleExample.createCriteria().andUserIdEqualTo(userId)
                    .andSourceIdEqualTo(orgId)
                    .andRoleIdIn(allRoleIds);
            userRoleMapper.deleteByExample(userRoleExample);
        }
    }

    public Integer checkSourceRole(String orgId, String userId, String roleId) {
        return extOrganizationMapper.checkSourceRole(orgId, userId, roleId);
    }

    public void checkOrgOwner(String organizationId) {
        SessionUser sessionUser = SessionUtils.getUser();
        UserDTO user = userService.getUserDTO(sessionUser.getId());
        List<String> collect = user.getUserRoles().stream()
                .filter(ur -> RoleConstants.ORG_ADMIN.equals(ur.getRoleId()) || RoleConstants.ORG_MEMBER.equals(ur.getRoleId()))
                .map(UserRole::getSourceId)
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
        final String orgId = group.getScopeId();
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
        List<Role> memberRoles = extUserRoleMapper.getOrganizationMemberRoles(orgId, userId);
        Organization user = organizationMapper.selectByPrimaryKey(orgId);
        if (user != null) {
            List<String> names = memberRoles.stream().map(Role::getName).collect(Collectors.toList());
            List<String> ids = memberRoles.stream().map(Role::getId).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), null, "用户 " + userId + " 修改角色为：" + String.join(",", names), user.getCreateUser(), null);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
