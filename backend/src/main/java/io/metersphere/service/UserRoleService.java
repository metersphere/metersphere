package io.metersphere.service;

import io.metersphere.base.domain.Role;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserRoleService {

    @Resource
    private ExtUserRoleMapper extUserRoleMapper;

    public List<Role> getOrganizationMemberRoles(String orgId, String userId) {
        return extUserRoleMapper.getOrganizationMemberRoles(orgId, userId);
    }

    public List<Role> getWorkspaceMemberRoles(String workspaceId, String userId) {
        return extUserRoleMapper.getWorkspaceMemberRoles(workspaceId, userId);
    }

}
