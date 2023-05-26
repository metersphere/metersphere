package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioEnvironment;
import io.metersphere.api.domain.ApiScenarioEnvironmentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioEnvironmentMapper {
    long countByExample(ApiScenarioEnvironmentExample example);

    int deleteByExample(ApiScenarioEnvironmentExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioEnvironment record);

    int insertSelective(ApiScenarioEnvironment record);

    List<ApiScenarioEnvironment> selectByExample(ApiScenarioEnvironmentExample example);

    ApiScenarioEnvironment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioEnvironment record, @Param("example") ApiScenarioEnvironmentExample example);

    int updateByExample(@Param("record") ApiScenarioEnvironment record, @Param("example") ApiScenarioEnvironmentExample example);

    int updateByPrimaryKeySelective(ApiScenarioEnvironment record);

    int updateByPrimaryKey(ApiScenarioEnvironment record);
}