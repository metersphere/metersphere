package io.metersphere.load.mapper;

import io.metersphere.load.domain.LoadTestReportResultRealtime;
import io.metersphere.load.domain.LoadTestReportResultRealtimeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestReportResultRealtimeMapper {
    long countByExample(LoadTestReportResultRealtimeExample example);

    int deleteByExample(LoadTestReportResultRealtimeExample example);

    int deleteByPrimaryKey(@Param("reportId") String reportId, @Param("reportKey") String reportKey, @Param("resourceIndex") Integer resourceIndex, @Param("sort") Integer sort);

    int insert(LoadTestReportResultRealtime record);

    int insertSelective(LoadTestReportResultRealtime record);

    List<LoadTestReportResultRealtime> selectByExampleWithBLOBs(LoadTestReportResultRealtimeExample example);

    List<LoadTestReportResultRealtime> selectByExample(LoadTestReportResultRealtimeExample example);

    LoadTestReportResultRealtime selectByPrimaryKey(@Param("reportId") String reportId, @Param("reportKey") String reportKey, @Param("resourceIndex") Integer resourceIndex, @Param("sort") Integer sort);

    int updateByExampleSelective(@Param("record") LoadTestReportResultRealtime record, @Param("example") LoadTestReportResultRealtimeExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportResultRealtime record, @Param("example") LoadTestReportResultRealtimeExample example);

    int updateByExample(@Param("record") LoadTestReportResultRealtime record, @Param("example") LoadTestReportResultRealtimeExample example);

    int updateByPrimaryKeySelective(LoadTestReportResultRealtime record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportResultRealtime record);
}