package io.metersphere.system.service;

import io.metersphere.sdk.dto.UserRoleRelationUserDTO;
import io.metersphere.sdk.dto.request.GlobalUserRoleRelationUpdateRequest;
import io.metersphere.sdk.service.BaseUserRoleRelationService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;
import io.metersphere.system.mapper.ExtUserRoleRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-6-12
 */
@Service
public class GlobalUserRoleRelationService extends BaseUserRoleRelationService {
    @Resource
    private ExtUserRoleRelationMapper extUserRoleRelationMapper;
    @Resource
    private GlobalUserRoleService globalUserRoleService;

    public List<UserRoleRelationUserDTO> list(GlobalUserRoleRelationQueryRequest request) {
        UserRole userRole = globalUserRoleService.get(request.getRoleId());
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        return extUserRoleRelationMapper.listGlobal(request);
    }

    public void add(GlobalUserRoleRelationUpdateRequest request) {
        UserRole userRole = globalUserRoleService.get(request.getRoleId());
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        request.getUserIds().forEach(userId -> {
            UserRoleRelation userRoleRelation = new UserRoleRelation();
            BeanUtils.copyBean(userRoleRelation, request);
            userRoleRelation.setUserId(userId);
            userRoleRelation.setSourceId(GlobalUserRoleService.SYSTEM_TYPE);
            super.add(userRoleRelation);
        });
    }

    @Override
    public void delete(String id) {
        UserRole userRole = getUserRole(id);
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        super.delete(id);
    }
}
