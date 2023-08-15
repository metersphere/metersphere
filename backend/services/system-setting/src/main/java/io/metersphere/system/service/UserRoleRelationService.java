package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.UserRoleEnum;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.*;
import io.metersphere.system.mapper.ExtUserRoleRelationMapper;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.response.user.UserTableResponse;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserRoleRelationService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private ExtUserRoleRelationMapper extUserRoleRelationMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private OperationLogService operationLogService;

    //批量添加用户记录日志
    public List<LogDTO> getBatchLogs(@Valid @NotEmpty List<String> userRoleId,
                                     @Valid User user,
                                     @Valid @NotEmpty String operationMethod,
                                     @Valid @NotEmpty String operator,
                                     @Valid @NotEmpty String operationType) {
        long operationTime = System.currentTimeMillis();
        List<LogDTO> logs = new ArrayList<>();
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andIdIn(userRoleId);
        List<UserRole> userRoleList = userRoleMapper.selectByExample(example);
        userRoleList.forEach(userRole -> {
            LogDTO log = new LogDTO();
            log.setId(UUID.randomUUID().toString());
            log.setProjectId(OperationLogConstants.SYSTEM);
            log.setOrganizationId(OperationLogConstants.SYSTEM);
            log.setType(operationType);
            log.setCreateUser(operator);
            log.setModule(OperationLogModule.SYSTEM_USER);
            log.setMethod(operationMethod);
            log.setCreateTime(operationTime);
            log.setSourceId(user.getId());
            log.setContent(user.getName() + StringUtils.SPACE
                    + operationType + StringUtils.SPACE
                    + "UserRole" + StringUtils.SPACE
                    + userRole.getName());
            log.setOriginalValue(JSON.toJSONBytes(userRole));
            logs.add(log);
        });
        return logs;
    }

    public void batchSave(@Validated({Created.class, Updated.class})
                          @NotEmpty(groups = {Created.class, Updated.class}, message = "{user_role.id.not_blank}")
                          List<@Valid @NotBlank(message = "{user_role.id.not_blank}", groups = {Created.class, Updated.class}) String> userRoleIdList,
                          @NotEmpty(groups = {Created.class, Updated.class}, message = "{user.info.not_empty}")
                          List<@Valid User> userList) {
        long operationTime = System.currentTimeMillis();
        List<UserRoleRelation> userRoleRelationSaveList = new ArrayList<>();
        //添加用户组织关系
        for (String userRoleId : userRoleIdList) {
            for (User user : userList) {
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setId(UUID.randomUUID().toString());
                userRoleRelation.setUserId(user.getId());
                userRoleRelation.setRoleId(userRoleId);
                userRoleRelation.setSourceId("system");
                userRoleRelation.setCreateTime(operationTime);
                userRoleRelation.setCreateUser(user.getCreateUser());
                userRoleRelationSaveList.add(userRoleRelation);
            }
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserRoleRelationMapper batchSaveMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        int insertIndex = 0;
        for (UserRoleRelation userRoleRelation : userRoleRelationSaveList) {
            batchSaveMapper.insert(userRoleRelation);
            insertIndex++;
            if (insertIndex % 50 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        //记录添加日志
        for (User user : userList) {
            operationLogService.batchAdd(this.getBatchLogs(userRoleIdList, user, "addUser", user.getCreateUser(), OperationLogType.ADD.name()));
        }

    }

    public Map<String, UserTableResponse> selectGlobalUserRoleAndOrganization(@Valid @NotEmpty List<String> userIdList) {
        List<UserRoleRelation> userRoleRelationList = extUserRoleRelationMapper.listByUserIdAndScope(userIdList);
        List<String> userRoleIdList = userRoleRelationList.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        List<String> sourceIdList = userRoleRelationList.stream().map(UserRoleRelation::getSourceId).collect(Collectors.toList());
        Map<String, UserRole> userRoleMap = new HashMap<>();
        Map<String, Organization> organizationMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userRoleIdList)) {
            UserRoleExample userRoleExample = new UserRoleExample();
            userRoleExample.createCriteria().andIdIn(userRoleIdList).andScopeIdEqualTo(UserRoleEnum.GLOBAL.toString());
            userRoleMap = userRoleMapper.selectByExample(userRoleExample).stream()
                    .collect(Collectors.toMap(UserRole::getId, item -> item));
        }
        if (CollectionUtils.isNotEmpty(sourceIdList)) {
            OrganizationExample organizationExample = new OrganizationExample();
            organizationExample.createCriteria().andIdIn(sourceIdList);
            organizationMap = organizationMapper.selectByExample(organizationExample).stream()
                    .collect(Collectors.toMap(Organization::getId, item -> item));
        }
        Map<String, UserTableResponse> returnMap = new HashMap<>();
        for (UserRoleRelation userRoleRelation : userRoleRelationList) {
            UserTableResponse userInfo = returnMap.get(userRoleRelation.getUserId());
            if (userInfo == null) {
                userInfo = new UserTableResponse();
                userInfo.setId(userRoleRelation.getUserId());
                returnMap.put(userRoleRelation.getUserId(), userInfo);
            }
            UserRole userRole = userRoleMap.get(userRoleRelation.getRoleId());
            Organization organization = organizationMap.get(userRoleRelation.getSourceId());
            userInfo.getUserRoleList().add(userRole);
            userInfo.getOrganizationList().add(organization);
        }
        return returnMap;
    }

    public List<UserRoleRelation> selectGlobalRoleByUserId(String userId) {
        return extUserRoleRelationMapper.selectGlobalRoleByUserId(userId);
    }

    public void updateUserSystemGlobalRole(@Valid User user, @Valid @NotEmpty String operator, @Valid @NotEmpty List<String> roleList) {
        //更新用户权限
        List<String> deleteRoleList = new ArrayList<>();
        List<UserRoleRelation> saveList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelationList = this.selectGlobalRoleByUserId(user.getId());
        List<String> userSavedRoleIdList = userRoleRelationList.stream().map(UserRoleRelation::getRoleId).toList();
        //获取要移除的权限
        for (String userSavedRoleId : userSavedRoleIdList) {
            if (!roleList.contains(userSavedRoleId)) {
                deleteRoleList.add(userSavedRoleId);
            }
        }
        //获取要添加的权限
        for (String roleId : roleList) {
            if (!userSavedRoleIdList.contains(roleId)) {
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setId(UUID.randomUUID().toString());
                userRoleRelation.setUserId(user.getId());
                userRoleRelation.setRoleId(roleId);
                userRoleRelation.setSourceId("system");
                userRoleRelation.setCreateTime(System.currentTimeMillis());
                userRoleRelation.setCreateUser(operator);
                saveList.add(userRoleRelation);
            }
        }

        if (CollectionUtils.isNotEmpty(deleteRoleList)) {
            List<String> deleteIdList = new ArrayList<>();
            userRoleRelationList.forEach(item -> {
                if (deleteRoleList.contains(item.getRoleId())) {
                    deleteIdList.add(item.getId());
                }
            });
            UserRoleRelationExample deleteExample = new UserRoleRelationExample();
            deleteExample.createCriteria().andIdIn(deleteIdList);
            userRoleRelationMapper.deleteByExample(deleteExample);
            //记录删除日志
            operationLogService.batchAdd(this.getBatchLogs(deleteRoleList, user, "updateUser", operator, OperationLogType.DELETE.name()));
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            //系统级权限不会太多，所以暂时不分批处理
            saveList.forEach(item -> userRoleRelationMapper.insert(item));
            //记录添加日志
            operationLogService.batchAdd(this.getBatchLogs(saveList.stream().map(UserRoleRelation::getRoleId).toList(),
                    user, "updateUser", operator, OperationLogType.ADD.name()));
        }
    }

    public List<UserRoleRelation> selectByUserId(String id) {
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(id);
        return userRoleRelationMapper.selectByExample(example);
    }

    public void deleteByUserIdList(@Valid @NotEmpty List<String> userIdList) {
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdIn(userIdList);
        userRoleRelationMapper.deleteByExample(example);
    }
}
