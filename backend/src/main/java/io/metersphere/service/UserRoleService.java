package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.UserGroupMapper;
import io.metersphere.base.mapper.UserRoleMapper;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserRoleService {

    @Resource
    private ExtUserRoleMapper extUserRoleMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserGroupMapper userGroupMapper;

    public List<Role> getOrganizationMemberRoles(String orgId, String userId) {
        return extUserRoleMapper.getOrganizationMemberRoles(orgId, userId);
    }

    public List<Role> getWorkspaceMemberRoles(String workspaceId, String userId) {
        return extUserRoleMapper.getWorkspaceMemberRoles(workspaceId, userId);
    }

    public List<Map<String, Object>> getUserRole(String userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        List<String> collect = userRoles.stream()
                .map(userRole -> userRole.getRoleId())
                .distinct()
                .collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            Map<String, Object> map = new HashMap<>(2);
            map.put("id", collect.get(i));
            map.put("ids", new ArrayList<>());
            for (int j = 0; j < userRoles.size(); j++) {
                String role = userRoles.get(j).getRoleId();
                if (StringUtils.equals(role, collect.get(i))) {
                    List ids = (List) map.get("ids");
                    ids.add(userRoles.get(j).getSourceId());
                }
            }
            list.add(map);
        }
        return list;
    }
    public List<Map<String, Object>> getUserGroup(String userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        UserGroupExample userRoleExample = new UserGroupExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userRoles = userGroupMapper.selectByExample(userRoleExample);
        List<String> collect = userRoles.stream()
                .map(userRole -> userRole.getGroupId())
                .distinct()
                .collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            Map<String, Object> map = new HashMap<>(2);
            map.put("id", collect.get(i));
            map.put("ids", new ArrayList<>());
            for (int j = 0; j < userRoles.size(); j++) {
                String role = userRoles.get(j).getGroupId();
                if (StringUtils.equals(role, collect.get(i))) {
                    List ids = (List) map.get("ids");
                    ids.add(userRoles.get(j).getSourceId());
                }
            }
            list.add(map);
        }
        return list;
    }
}
