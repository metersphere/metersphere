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
                .map(UserGroup::getGroupId)
                .distinct()
                .collect(Collectors.toList());
        for (String id : collect) {
            Map<String, Object> map = new HashMap<>(2);
            map.put("id", id);
            map.put("ids", new ArrayList<>());
            for (UserGroup userGroup : userGroups) {
                String groupId = userGroup.getGroupId();
                if (StringUtils.equals(groupId, id)) {
                    List ids = (List) map.get("ids");
                    ids.add(userGroup.getSourceId());
                }
            }
            list.add(map);
        }
        return list;
    }
}
