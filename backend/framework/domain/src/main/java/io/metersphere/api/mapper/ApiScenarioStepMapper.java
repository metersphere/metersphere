package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioStep;
import io.metersphere.api.domain.ApiScenarioStepExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioStepMapper {
    long countByExample(ApiScenarioStepExample example);

    int deleteByExample(ApiScenarioStepExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioStep record);

    int insertSelective(ApiScenarioStep record);

    List<ApiScenarioStep> selectByExample(ApiScenarioStepExample example);

    ApiScenarioStep selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioStep record, @Param("example") ApiScenarioStepExample example);

    int updateByExample(@Param("record") ApiScenarioStep record, @Param("example") ApiScenarioStepExample example);

    int updateByPrimaryKeySelective(ApiScenarioStep record);

    int updateByPrimaryKey(ApiScenarioStep record);

    int batchInsert(@Param("list") List<ApiScenarioStep> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioStep> list, @Param("selective") ApiScenarioStep.Column ... selective);
}