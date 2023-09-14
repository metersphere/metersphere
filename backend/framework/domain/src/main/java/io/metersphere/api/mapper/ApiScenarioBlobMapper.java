package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioBlob;
import io.metersphere.api.domain.ApiScenarioBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioBlobMapper {
    long countByExample(ApiScenarioBlobExample example);

    int deleteByExample(ApiScenarioBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioBlob record);

    int insertSelective(ApiScenarioBlob record);

    List<ApiScenarioBlob> selectByExampleWithBLOBs(ApiScenarioBlobExample example);

    List<ApiScenarioBlob> selectByExample(ApiScenarioBlobExample example);

    ApiScenarioBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioBlob record, @Param("example") ApiScenarioBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioBlob record, @Param("example") ApiScenarioBlobExample example);

    int updateByExample(@Param("record") ApiScenarioBlob record, @Param("example") ApiScenarioBlobExample example);

    int updateByPrimaryKeySelective(ApiScenarioBlob record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioBlob record);

    int batchInsert(@Param("list") List<ApiScenarioBlob> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioBlob> list, @Param("selective") ApiScenarioBlob.Column ... selective);
}