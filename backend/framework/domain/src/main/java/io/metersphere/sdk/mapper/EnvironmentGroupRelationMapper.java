package io.metersphere.sdk.mapper;

import io.metersphere.sdk.domain.EnvironmentGroupRelation;
import io.metersphere.sdk.domain.EnvironmentGroupRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EnvironmentGroupRelationMapper {
    long countByExample(EnvironmentGroupRelationExample example);

    int deleteByExample(EnvironmentGroupRelationExample example);

    int deleteByPrimaryKey(String id);

    int insert(EnvironmentGroupRelation record);

    int insertSelective(EnvironmentGroupRelation record);

    List<EnvironmentGroupRelation> selectByExample(EnvironmentGroupRelationExample example);

    EnvironmentGroupRelation selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") EnvironmentGroupRelation record, @Param("example") EnvironmentGroupRelationExample example);

    int updateByExample(@Param("record") EnvironmentGroupRelation record, @Param("example") EnvironmentGroupRelationExample example);

    int updateByPrimaryKeySelective(EnvironmentGroupRelation record);

    int updateByPrimaryKey(EnvironmentGroupRelation record);

    int batchInsert(@Param("list") List<EnvironmentGroupRelation> list);

    int batchInsertSelective(@Param("list") List<EnvironmentGroupRelation> list, @Param("selective") EnvironmentGroupRelation.Column ... selective);
}