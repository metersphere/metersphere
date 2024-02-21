package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiTestCaseRecord;
import io.metersphere.api.domain.ApiTestCaseRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestCaseRecordMapper {
    long countByExample(ApiTestCaseRecordExample example);

    int deleteByExample(ApiTestCaseRecordExample example);

    int deleteByPrimaryKey(@Param("apiReportId") String apiReportId, @Param("apiTestCaseId") String apiTestCaseId);

    int insert(ApiTestCaseRecord record);

    int insertSelective(ApiTestCaseRecord record);

    List<ApiTestCaseRecord> selectByExample(ApiTestCaseRecordExample example);

    int updateByExampleSelective(@Param("record") ApiTestCaseRecord record, @Param("example") ApiTestCaseRecordExample example);

    int updateByExample(@Param("record") ApiTestCaseRecord record, @Param("example") ApiTestCaseRecordExample example);

    int batchInsert(@Param("list") List<ApiTestCaseRecord> list);

    int batchInsertSelective(@Param("list") List<ApiTestCaseRecord> list, @Param("selective") ApiTestCaseRecord.Column ... selective);
}