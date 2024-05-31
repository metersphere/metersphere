package io.metersphere.system.service;

import io.metersphere.sdk.constants.UserRoleScope;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;
import io.metersphere.system.dto.request.user.UserRoleBatchRelationRequest;
import io.metersphere.system.dto.sdk.request.GlobalUserRoleRelationUpdateRequest;
import io.metersphere.system.dto.table.TableBatchProcessResponse;
import io.metersphere.system.dto.user.UserExcludeOptionDTO;
import io.metersphere.system.dto.user.UserRoleRelationUserDTO;
import io.metersphere.system.mapper.ExtUserRoleRelationMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private SimpleUserService simpleUserService;
    @Resource
    private UserToolService userToolService;
    @Resource
    private BaseUserRoleService baseUserRoleService;

    public List<UserRoleRelationUserDTO> list(GlobalUserRoleRelationQueryRequest request) {
        List<UserRoleRelationUserDTO> userRoleRelationUserDTOS = extUserRoleRelationMapper.listGlobal(request);
        UserRole userRole = globalUserRoleService.get(request.getRoleId());
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        return userRoleRelationUserDTOS;
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
        simpleUserService.checkUserLegality(request.getUserIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getUserIds().forEach(userId -> {
            UserRoleRelation userRoleRelation = new UserRoleRelation();
            BeanUtils.copyBean(userRoleRelation, request);
            userRoleRelation.setUserId(userId);
            userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
            checkExist(userRoleRelation);
            userRoleRelation.setCreateTime(System.currentTimeMillis());
            userRoleRelation.setId(IDGenerator.nextStr());
            userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
            userRoleRelations.add(userRoleRelation);
        });
        userRoleRelationMapper.batchInsert(userRoleRelations);
    }

    public List<UserRoleRelation> selectByUserIdAndRuleId(List<String> userIds, List<String> roleIds) {
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdIn(userIds).andRoleIdIn(roleIds);

        return userRoleRelationMapper.selectByExample(example);
    }

    public TableBatchProcessResponse batchAdd(@Validated({Created.class, Updated.class}) UserRoleBatchRelationRequest request, String operator) {
        //检查角色的合法性
        this.checkGlobalSystemUserRoleLegality(request.getRoleIds());
        //获取本次处理的用户
        request.setSelectIds(userToolService.getBatchUserIds(request));
        //检查用户的合法性
        simpleUserService.checkUserLegality(request.getSelectIds());
        List<UserRoleRelation> savedUserRoleRelation = this.selectByUserIdAndRuleId(request.getSelectIds(), request.getRoleIds());
        //过滤已经存储过的用户关系
        Map<String, List<String>> userRoleIdMap = savedUserRoleRelation.stream()
                .collect(Collectors.groupingBy(UserRoleRelation::getUserId, Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        long createTime = System.currentTimeMillis();
        List<UserRoleRelation> saveList = new ArrayList<>();
        for (String userId : request.getSelectIds()) {
            for (String roleId : request.getRoleIds()) {
                if (userRoleIdMap.containsKey(userId) && userRoleIdMap.get(userId).contains(roleId)) {
                    continue;
                }
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(userId);
                userRoleRelation.setRoleId(roleId);
                userRoleRelation.setCreateUser(operator);
                userRoleRelation.setCreateTime(createTime);
                userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
                userRoleRelation.setId(IDGenerator.nextStr());
                userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
                saveList.add(userRoleRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            userRoleRelationMapper.batchInsert(saveList);
        }
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(request.getSelectIds().size());
        response.setSuccessCount(saveList.size());
        return response;
    }

    @Override
    public void delete(String id) {
        UserRole userRole = getUserRole(id);
        baseUserRoleService.checkResourceExist(userRole);
        UserRoleRelation userRoleRelation = userRoleRelationMapper.selectByPrimaryKey(id);
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        super.delete(id);
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria()
                .andUserIdEqualTo(userRoleRelation.getUserId())
                .andSourceIdEqualTo(UserRoleScope.SYSTEM);
        if (CollectionUtils.isEmpty(userRoleRelationMapper.selectByExample(example))) {
            throw new MSException(GLOBAL_USER_ROLE_LIMIT);
        }
    }

    public List<UserExcludeOptionDTO> getExcludeSelectOption(String roleId, String keyword) {
        baseUserRoleService.getWithCheck(roleId);
        return super.getExcludeSelectOptionWithLimit(roleId, keyword);
    }
}
