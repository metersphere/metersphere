package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.OrganizationMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.UserRoleMapper;
import io.metersphere.base.mapper.ext.ExtOrganizationMapper;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import io.metersphere.dto.OrganizationMemberDTO;
import io.metersphere.dto.UserRoleHelpDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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

    public Organization addOrganization(Organization organization) {
        long currentTimeMillis = System.currentTimeMillis();
        organization.setId(UUID.randomUUID().toString());
        organization.setCreateTime(currentTimeMillis);
        organization.setUpdateTime(currentTimeMillis);
        organizationMapper.insertSelective(organization);
        return organization;
    }

    public List<Organization> getOrganizationList() {
        return organizationMapper.selectByExample(null);
    }

    public void deleteOrganization(String organizationId) {
        organizationMapper.deleteByPrimaryKey(organizationId);
    }

    public void updateOrganization(Organization organization) {
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
        OrganizationExample organizationExample = new OrganizationExample();
        organizationExample.createCriteria().andIdIn(list);
        return organizationMapper.selectByExample(organizationExample);
    }

    public void updateOrgMember(OrganizationMemberDTO memberDTO) {
        User user = new User();
        BeanUtils.copyProperties(memberDTO, user);
        userMapper.updateByPrimaryKeySelective(user);
        //
        String orgId = memberDTO.getOrganizationId();
        String userId = user.getId();
        // 已有角色
        List<Role> memberRoles = extUserRoleMapper.getOrganizationMemberRoles(orgId, userId);
        // 修改后的角色
        List<String> roles = memberDTO.getRoleIds();
        List<String> allRoleIds = memberRoles.stream().map(Role::getId).collect(Collectors.toList());
        // 更新用户时添加了角色
        if (roles.size() > allRoleIds.size()) {
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
        } else if (roles.size() < allRoleIds.size()){
            allRoleIds.removeAll(roles);
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
}
