package io.metersphere.system.service;

import io.metersphere.sdk.constants.UserRoleEnum;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.response.UserInfo;
import io.metersphere.system.mapper.ExtUserRoleRelationMapper;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections4.CollectionUtils;
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

    public void batchSave(@Validated({Created.class, Updated.class})
                          @NotEmpty(groups = {Created.class, Updated.class}, message = "{user.organizationId.not_blank}")
                          List<@Valid @NotBlank(message = "{user.organizationId.not_blank}", groups = {Created.class, Updated.class}) String> organizationIdList,
                          @NotEmpty(groups = {Created.class, Updated.class}, message = "{user_role.id.not_blank}")
                          List<@Valid @NotBlank(message = "{user_role.id.not_blank}", groups = {Created.class, Updated.class}) String> userRoleIdList,
                          @NotEmpty(groups = {Created.class, Updated.class}, message = "{user.info.not_empty}")
                          List<@Valid User> userInfoList) {
        long operationTime = System.currentTimeMillis();
        List<UserRoleRelation> userRoleRelationSaveList = new ArrayList<>();
        //添加用户组织关系
        for (String orgId : organizationIdList) {
            for (String userRoleId : userRoleIdList) {
                for (User user : userInfoList) {
                    UserRoleRelation userRoleRelation = new UserRoleRelation();
                    userRoleRelation.setId(UUID.randomUUID().toString());
                    userRoleRelation.setUserId(user.getId());
                    userRoleRelation.setRoleId(userRoleId);
                    userRoleRelation.setSourceId(orgId);
                    userRoleRelation.setCreateTime(operationTime);
                    userRoleRelation.setCreateUser(user.getCreateUser());
                    userRoleRelationSaveList.add(userRoleRelation);
                }
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
    }

    public Map<String, UserInfo> selectGlobalUserRoleAndOrganization(@Valid @NotEmpty List<String> userIdList) {
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
        Map<String, UserInfo> returnMap = new HashMap<>();
        for (UserRoleRelation userRoleRelation : userRoleRelationList) {
            UserInfo userInfo = returnMap.get(userRoleRelation.getUserId());
            if (userInfo == null) {
                userInfo = new UserInfo();
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
}
