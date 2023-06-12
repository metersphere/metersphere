package io.metersphere.system.service;

import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserRoleRelationService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    private UserRoleRelationMapper userRoleRelationMapper;

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
        try {
            int insertIndex = 0;
            for (UserRoleRelation userRoleRelation : userRoleRelationSaveList) {
                batchSaveMapper.insert(userRoleRelation);
                insertIndex++;
                if (insertIndex % 50 == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }
}
