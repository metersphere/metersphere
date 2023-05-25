package io.metersphere.load.mapper;

import io.metersphere.load.domain.LoadTestReportResult;
import io.metersphere.load.domain.LoadTestReportResultExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestReportResultMapper {
    long countByExample(LoadTestReportResultExample example);

    int deleteByExample(LoadTestReportResultExample example);

    int deleteByPrimaryKey(String id);

    int insert(LoadTestReportResult record);

    int insertSelective(LoadTestReportResult record);

    List<LoadTestReportResult> selectByExampleWithBLOBs(LoadTestReportResultExample example);

    List<LoadTestReportResult> selectByExample(LoadTestReportResultExample example);

    LoadTestReportResult selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LoadTestReportResult record, @Param("example") LoadTestReportResultExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportResult record, @Param("example") LoadTestReportResultExample example);

    int updateByExample(@Param("record") LoadTestReportResult record, @Param("example") LoadTestReportResultExample example);

    int updateByPrimaryKeySelective(LoadTestReportResult record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportResult record);

    int updateByPrimaryKey(LoadTestReportResult record);
}