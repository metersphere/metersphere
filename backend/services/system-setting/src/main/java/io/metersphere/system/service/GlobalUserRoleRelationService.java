package io.metersphere.system.service;

import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.GlobalUserRoleUserDTO;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import java.util.List;
import java.util.ArrayList;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;
/**
 * @author jianxing
 * @date : 2023-6-12
 */
@Service
public class GlobalUserRoleRelationService {

    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    public List<GlobalUserRoleUserDTO> list(GlobalUserRoleRelationQueryRequest request) {
        return new ArrayList<>();
    }

    public UserRoleRelation add(UserRoleRelation userRoleRelation) {
        userRoleRelationMapper.insert(userRoleRelation);
        return userRoleRelation;
    }

    public String delete(String id) {
        userRoleRelationMapper.deleteByPrimaryKey(id);
        return id;
    }
}
