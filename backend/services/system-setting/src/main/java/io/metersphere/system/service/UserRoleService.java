package io.metersphere.system.service;

import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.mapper.ExtUserMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserRoleService {
    @Resource
    private ExtUserMapper extUserMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    public List<UserRole> selectByUserRoleRelations(List<UserRoleRelation> userRoleRelations) {
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            List<String> userRoleIds = userRoleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
            UserRoleExample example = new UserRoleExample();
            example.createCriteria().andIdIn(userRoleIds);
            return userRoleMapper.selectByExample(example);
        } else {
            return new ArrayList<>();
        }
    }

    public List<UserExtendDTO> getMember(String sourceId, String roleId, String keyword) {
        List<UserExtendDTO> userExtendDTOS = new ArrayList<>();
        // 查询组织或项目下所有用户关系
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(sourceId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            Map<String, List<String>> userRoleMap = userRoleRelations.stream().collect(Collectors.groupingBy(UserRoleRelation::getUserId,
                    Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
            userRoleMap.forEach((k, v) -> {
                UserExtendDTO userExtendDTO = new UserExtendDTO();
                userExtendDTO.setId(k);
                v.forEach(roleItem -> {
                    if (StringUtils.equals(roleItem, roleId)) {
                        // 该用户已存在用户组关系, 设置为选中状态
                        userExtendDTO.setCheckRoleFlag(true);
                    }
                });
                userExtendDTOS.add(userExtendDTO);
            });
            // 设置用户信息, 用户不存在或者已删除, 则不展示
            List<String> userIds = userExtendDTOS.stream().map(UserExtendDTO::getId).toList();
            List<User> users = extUserMapper.getRoleUserByParam(userIds, keyword);
            if (CollectionUtils.isNotEmpty(users)) {
                Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
                userExtendDTOS.removeIf(userExtend -> {
                    if (userMap.containsKey(userExtend.getId())) {
                        BeanUtils.copyBean(userExtend, userMap.get(userExtend.getId()));
                        return false;
                    }
                    return true;
                });
            } else {
                userExtendDTOS.clear();
            }
        }

        userExtendDTOS.sort(Comparator.comparing(UserExtendDTO::getName));
        return userExtendDTOS;
    }
}
