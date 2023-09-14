package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioReference;
import io.metersphere.api.domain.ApiScenarioReferenceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReferenceMapper {
    long countByExample(ApiScenarioReferenceExample example);

    int deleteByExample(ApiScenarioReferenceExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioReference record);

    int insertSelective(ApiScenarioReference record);

    List<ApiScenarioReference> selectByExample(ApiScenarioReferenceExample example);

    ApiScenarioReference selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioReference record, @Param("example") ApiScenarioReferenceExample example);

    int updateByExample(@Param("record") ApiScenarioReference record, @Param("example") ApiScenarioReferenceExample example);

    int updateByPrimaryKeySelective(ApiScenarioReference record);

    int updateByPrimaryKey(ApiScenarioReference record);

    int batchInsert(@Param("list") List<ApiScenarioReference> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioReference> list, @Param("selective") ApiScenarioReference.Column ... selective);
}