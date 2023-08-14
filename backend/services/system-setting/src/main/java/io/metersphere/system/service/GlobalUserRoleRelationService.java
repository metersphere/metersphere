package io.metersphere.system.service;

import io.metersphere.sdk.dto.UserRoleRelationUserDTO;
import io.metersphere.sdk.dto.request.GlobalUserRoleRelationUpdateRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.service.BaseUserRoleRelationService;
import io.metersphere.sdk.service.BaseUserRoleService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;
import io.metersphere.system.dto.request.user.UserAndRoleBatchRequest;
import io.metersphere.system.dto.response.UserBatchProcessResponse;
import io.metersphere.system.mapper.ExtUserRoleRelationMapper;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.result.SystemResultCode.GLOBAL_USER_ROLE_LIMIT;

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
    @Resource
    private UserService userService;

    public List<UserRoleRelationUserDTO> list(GlobalUserRoleRelationQueryRequest request) {
        UserRole userRole = globalUserRoleService.get(request.getRoleId());
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        return extUserRoleRelationMapper.listGlobal(request);
    }

    //校验用户组
    private void checkGlobalSystemUserRoleLegality(List<String> checkIdList) {
        List<UserRole> userRoleList = globalUserRoleService.getList(checkIdList);
        if (userRoleList.size() != checkIdList.size()) {
            throw new MSException(Translator.get("user_role_not_exist"));
        }
        userRoleList.forEach(userRole -> {
            globalUserRoleService.checkSystemUserGroup(userRole);
            globalUserRoleService.checkGlobalUserRole(userRole);
        });
    }

    public void add(GlobalUserRoleRelationUpdateRequest request) {
        this.checkGlobalSystemUserRoleLegality(
                Collections.singletonList(request.getRoleId()));
        //检查用户的合法性
        userService.checkUserLegality(request.getUserIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getUserIds().forEach(userId -> {
            UserRoleRelation userRoleRelation = new UserRoleRelation();
            BeanUtils.copyBean(userRoleRelation, request);
            userRoleRelation.setUserId(userId);
            userRoleRelation.setSourceId(GlobalUserRoleService.SYSTEM_TYPE);
            checkExist(userRoleRelation);
            userRoleRelation.setCreateTime(System.currentTimeMillis());
            userRoleRelation.setId(UUID.randomUUID().toString());
            userRoleRelations.add(userRoleRelation);
        });
        userRoleRelationMapper.batchInsert(userRoleRelations);
    }

    public List<UserRoleRelation> selectByUserIdAndRuleId(List<String> userIds, List<String> roleIds) {
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdIn(userIds).andRoleIdIn(roleIds);

        return userRoleRelationMapper.selectByExample(example);
    }

    public UserBatchProcessResponse batchAdd(@Validated({Created.class, Updated.class}) UserAndRoleBatchRequest request, String operator) {
        //检查角色的合法性
        this.checkGlobalSystemUserRoleLegality(request.getRoleIds());
        //获取本次处理的用户
        request.setUserIds(userService.getBatchUserIds(request));
        //检查用户的合法性
        userService.checkUserLegality(request.getUserIds());
        List<UserRoleRelation> savedUserRoleRelation = this.selectByUserIdAndRuleId(request.getUserIds(), request.getRoleIds());
        //过滤已经存储过的用户关系
        Map<String, List<String>> userRoleIdMap = savedUserRoleRelation.stream()
                .collect(Collectors.groupingBy(UserRoleRelation::getUserId, Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        long createTime = System.currentTimeMillis();
        List<UserRoleRelation> saveList = new ArrayList<>();
        for (String userId : request.getUserIds()) {
            for (String roleId : request.getRoleIds()) {
                if (userRoleIdMap.containsKey(userId) && userRoleIdMap.get(userId).contains(roleId)) {
                    continue;
                }
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(userId);
                userRoleRelation.setRoleId(roleId);
                userRoleRelation.setCreateUser(operator);
                userRoleRelation.setCreateTime(createTime);
                userRoleRelation.setSourceId(GlobalUserRoleService.SYSTEM_TYPE);
                userRoleRelation.setId(UUID.randomUUID().toString());
                saveList.add(userRoleRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            userRoleRelationMapper.batchInsert(saveList);
        }
        UserBatchProcessResponse response = new UserBatchProcessResponse();
        response.setTotalCount(request.getUserIds().size());
        response.setSuccessCount(saveList.size());
        return response;
    }

    @Override
    public void delete(String id) {
        UserRole userRole = getUserRole(id);
        UserRoleRelation userRoleRelation = userRoleRelationMapper.selectByPrimaryKey(id);
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        super.delete(id);
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria()
                .andUserIdEqualTo(userRoleRelation.getUserId())
                .andSourceIdEqualTo(BaseUserRoleService.SYSTEM_TYPE);
        if (CollectionUtils.isEmpty(userRoleRelationMapper.selectByExample(example))) {
            throw new MSException(GLOBAL_USER_ROLE_LIMIT);
        }
    }
}
