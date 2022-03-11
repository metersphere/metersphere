package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiDefinitionScenarioRelevance;
import io.metersphere.base.domain.ApiDefinitionScenarioRelevanceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionScenarioRelevanceMapper {
    long countByExample(ApiDefinitionScenarioRelevanceExample example);

    int deleteByExample(ApiDefinitionScenarioRelevanceExample example);

    int deleteByPrimaryKey(String reportId);

    int insert(ApiDefinitionScenarioRelevance record);

    int insertSelective(ApiDefinitionScenarioRelevance record);

    List<ApiDefinitionScenarioRelevance> selectByExample(ApiDefinitionScenarioRelevanceExample example);

    int updateByExampleSelective(@Param("record") ApiDefinitionScenarioRelevance record, @Param("example") ApiDefinitionScenarioRelevanceExample example);

    int updateByExample(@Param("record") ApiDefinitionScenarioRelevance record, @Param("example") ApiDefinitionScenarioRelevanceExample example);
}