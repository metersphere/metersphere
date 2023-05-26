package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioMapper {
    long countByExample(ApiScenarioExample example);

    int deleteByExample(ApiScenarioExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenario record);

    int insertSelective(ApiScenario record);

    List<ApiScenario> selectByExample(ApiScenarioExample example);

    ApiScenario selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenario record, @Param("example") ApiScenarioExample example);

    int updateByExample(@Param("record") ApiScenario record, @Param("example") ApiScenarioExample example);

    int updateByPrimaryKeySelective(ApiScenario record);

    int updateByPrimaryKey(ApiScenario record);
}