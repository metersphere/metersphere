package io.metersphere.system.service;

import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleExample;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.mapper.UserRoleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserRoleService {
    @Resource
    private UserRoleMapper userRoleMapper;

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
}
