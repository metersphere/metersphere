package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiScenarioReferenceId;
import io.metersphere.base.domain.ApiScenarioReferenceIdExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReferenceIdMapper {
    long countByExample(ApiScenarioReferenceIdExample example);

    int deleteByExample(ApiScenarioReferenceIdExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioReferenceId record);

    int insertSelective(ApiScenarioReferenceId record);

    List<ApiScenarioReferenceId> selectByExample(ApiScenarioReferenceIdExample example);

    ApiScenarioReferenceId selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioReferenceId record, @Param("example") ApiScenarioReferenceIdExample example);

    int updateByExample(@Param("record") ApiScenarioReferenceId record, @Param("example") ApiScenarioReferenceIdExample example);

    int updateByPrimaryKeySelective(ApiScenarioReferenceId record);

    int updateByPrimaryKey(ApiScenarioReferenceId record);
}