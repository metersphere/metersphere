package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiSyncRuleRelation;
import io.metersphere.base.domain.ApiSyncRuleRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiSyncRuleRelationMapper {
    long countByExample(ApiSyncRuleRelationExample example);

    int deleteByExample(ApiSyncRuleRelationExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiSyncRuleRelation record);

    int insertSelective(ApiSyncRuleRelation record);

    List<ApiSyncRuleRelation> selectByExampleWithBLOBs(ApiSyncRuleRelationExample example);

    List<ApiSyncRuleRelation> selectByExample(ApiSyncRuleRelationExample example);

    ApiSyncRuleRelation selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiSyncRuleRelation record, @Param("example") ApiSyncRuleRelationExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiSyncRuleRelation record, @Param("example") ApiSyncRuleRelationExample example);

    int updateByExample(@Param("record") ApiSyncRuleRelation record, @Param("example") ApiSyncRuleRelationExample example);

    int updateByPrimaryKeySelective(ApiSyncRuleRelation record);

    int updateByPrimaryKeyWithBLOBs(ApiSyncRuleRelation record);

    int updateByPrimaryKey(ApiSyncRuleRelation record);
}