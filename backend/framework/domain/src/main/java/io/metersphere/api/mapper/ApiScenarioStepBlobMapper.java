package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioStepBlob;
import io.metersphere.api.domain.ApiScenarioStepBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioStepBlobMapper {
    long countByExample(ApiScenarioStepBlobExample example);

    int deleteByExample(ApiScenarioStepBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioStepBlob record);

    int insertSelective(ApiScenarioStepBlob record);

    List<ApiScenarioStepBlob> selectByExampleWithBLOBs(ApiScenarioStepBlobExample example);

    List<ApiScenarioStepBlob> selectByExample(ApiScenarioStepBlobExample example);

    ApiScenarioStepBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioStepBlob record, @Param("example") ApiScenarioStepBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioStepBlob record, @Param("example") ApiScenarioStepBlobExample example);

    int updateByExample(@Param("record") ApiScenarioStepBlob record, @Param("example") ApiScenarioStepBlobExample example);

    int updateByPrimaryKeySelective(ApiScenarioStepBlob record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioStepBlob record);

    int updateByPrimaryKey(ApiScenarioStepBlob record);

    int batchInsert(@Param("list") List<ApiScenarioStepBlob> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioStepBlob> list, @Param("selective") ApiScenarioStepBlob.Column ... selective);
}