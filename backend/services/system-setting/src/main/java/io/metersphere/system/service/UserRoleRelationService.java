package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.UserRoleEnum;
import io.metersphere.sdk.constants.UserRoleScope;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.user.response.UserTableResponse;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.ExtUserRoleRelationMapper;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Resource
    private ProjectMapper projectMapper;

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
            log.setProjectId(OperationLogConstants.SYSTEM);
            log.setOrganizationId(OperationLogConstants.SYSTEM);
            log.setType(operationType);
            log.setCreateUser(operator);
            log.setModule(OperationLogModule.SETTING_SYSTEM_USER_SINGLE);
            log.setMethod(operationMethod);
            log.setCreateTime(operationTime);
            log.setSourceId(user.getId());
            log.setContent(user.getName() + StringUtils.SPACE
                    + Translator.get(StringUtils.lowerCase(operationType)) + StringUtils.SPACE
                    + Translator.get("permission.project_group.name") + StringUtils.SPACE
                    + userRole.getName());
            log.setOriginalValue(JSON.toJSONBytes(userRole));
            logs.add(log);
        });
        return logs;
    }

    public Map<Organization, List<Project>> selectOrganizationProjectByUserId(String userId) {
        Map<Organization, List<Project>> returnMap = new LinkedHashMap<>();
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<UserRoleRelation> userRoleRelationList = userRoleRelationMapper.selectByExample(example);
        for (UserRoleRelation userRoleRelation : userRoleRelationList) {
            Organization organization = organizationMapper.selectByPrimaryKey(userRoleRelation.getOrganizationId());
            if (organization != null) {
                returnMap.computeIfAbsent(organization, k -> new ArrayList<>());
                Project project = projectMapper.selectByPrimaryKey(userRoleRelation.getSourceId());
                if (project != null && !returnMap.get(organization).contains(project)) {
                    returnMap.get(organization).add(project);
                }
            }
        }
        return returnMap;
    }

    public Map<String, UserTableResponse> selectGlobalUserRoleAndOrganization(@Valid @NotEmpty List<String> userIdList) {
        List<UserRoleRelation> userRoleRelationList = extUserRoleRelationMapper.selectRoleByUserIdList(userIdList);
        List<String> userRoleIdList = userRoleRelationList.stream().map(UserRoleRelation::getRoleId).distinct().collect(Collectors.toList());
        List<String> sourceIdList = userRoleRelationList.stream().map(UserRoleRelation::getSourceId).distinct().collect(Collectors.toList());
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
            if (userRole != null && StringUtils.equalsIgnoreCase(userRole.getType(), UserRoleScope.SYSTEM)) {
                userInfo.setUserRole(userRole);
            }
            Organization organization = organizationMap.get(userRoleRelation.getSourceId());
            if (organization != null && !userInfo.getOrganizationList().contains(organization)) {
                userInfo.setOrganization(organization);
            }
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
                userRoleRelation.setId(IDGenerator.nextStr());
                userRoleRelation.setUserId(user.getId());
                userRoleRelation.setRoleId(roleId);
                userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
                userRoleRelation.setCreateTime(System.currentTimeMillis());
                userRoleRelation.setCreateUser(operator);
                userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
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
