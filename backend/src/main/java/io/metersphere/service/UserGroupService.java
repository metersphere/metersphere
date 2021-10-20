package io.metersphere.service;

import io.metersphere.base.domain.UserGroup;
import io.metersphere.base.domain.UserGroupExample;
import io.metersphere.base.mapper.UserGroupMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserGroupService {
    @Resource
    private UserGroupMapper userGroupMapper;

    public List<Map<String, Object>> getUserGroup(String userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        UserGroupExample userRoleExample = new UserGroupExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userRoleExample);
        List<String> collect = userGroups.stream()
                .map(userRole -> userRole.getGroupId())
                .distinct()
                .collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            Map<String, Object> map = new HashMap<>(2);
            map.put("id", collect.get(i));
            map.put("ids", new ArrayList<>());
            for (int j = 0; j < userGroups.size(); j++) {
                String role = userGroups.get(j).getGroupId();
                if (StringUtils.equals(role, collect.get(i))) {
                    List ids = (List) map.get("ids");
                    ids.add(userGroups.get(j).getSourceId());
                }
            }
            list.add(map);
        }
        return list;
    }
}
