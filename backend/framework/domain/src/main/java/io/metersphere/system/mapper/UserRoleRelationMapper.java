package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserRoleRelationMapper {
    long countByExample(UserRoleRelationExample example);

    int deleteByExample(UserRoleRelationExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserRoleRelation record);

    int insertSelective(UserRoleRelation record);

    List<UserRoleRelation> selectByExample(UserRoleRelationExample example);

    UserRoleRelation selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UserRoleRelation record, @Param("example") UserRoleRelationExample example);

    int updateByExample(@Param("record") UserRoleRelation record, @Param("example") UserRoleRelationExample example);

    int updateByPrimaryKeySelective(UserRoleRelation record);

    int updateByPrimaryKey(UserRoleRelation record);

    int batchInsert(@Param("list") List<UserRoleRelation> list);

    int batchInsertSelective(@Param("list") List<UserRoleRelation> list, @Param("selective") UserRoleRelation.Column ... selective);
}