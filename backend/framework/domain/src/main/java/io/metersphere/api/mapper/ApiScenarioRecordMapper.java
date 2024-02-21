package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioRecord;
import io.metersphere.api.domain.ApiScenarioRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioRecordMapper {
    long countByExample(ApiScenarioRecordExample example);

    int deleteByExample(ApiScenarioRecordExample example);

    int deleteByPrimaryKey(@Param("apiScenarioReportId") String apiScenarioReportId, @Param("apiScenarioId") String apiScenarioId);

    int insert(ApiScenarioRecord record);

    int insertSelective(ApiScenarioRecord record);

    List<ApiScenarioRecord> selectByExample(ApiScenarioRecordExample example);

    int updateByExampleSelective(@Param("record") ApiScenarioRecord record, @Param("example") ApiScenarioRecordExample example);

    int updateByExample(@Param("record") ApiScenarioRecord record, @Param("example") ApiScenarioRecordExample example);

    int batchInsert(@Param("list") List<ApiScenarioRecord> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioRecord> list, @Param("selective") ApiScenarioRecord.Column ... selective);
}