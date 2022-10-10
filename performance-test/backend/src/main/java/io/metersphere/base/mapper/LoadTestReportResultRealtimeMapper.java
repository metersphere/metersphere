package io.metersphere.base.mapper;

import io.metersphere.base.domain.LoadTestReportResultRealtime;
import io.metersphere.base.domain.LoadTestReportResultRealtimeExample;
import io.metersphere.base.domain.LoadTestReportResultRealtimeKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoadTestReportResultRealtimeMapper {
    long countByExample(LoadTestReportResultRealtimeExample example);

    int deleteByExample(LoadTestReportResultRealtimeExample example);

    int deleteByPrimaryKey(LoadTestReportResultRealtimeKey key);

    int insert(LoadTestReportResultRealtime record);

    int insertSelective(LoadTestReportResultRealtime record);

    List<LoadTestReportResultRealtime> selectByExampleWithBLOBs(LoadTestReportResultRealtimeExample example);

    List<LoadTestReportResultRealtime> selectByExample(LoadTestReportResultRealtimeExample example);

    LoadTestReportResultRealtime selectByPrimaryKey(LoadTestReportResultRealtimeKey key);

    int updateByExampleSelective(@Param("record") LoadTestReportResultRealtime record, @Param("example") LoadTestReportResultRealtimeExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportResultRealtime record, @Param("example") LoadTestReportResultRealtimeExample example);

    int updateByExample(@Param("record") LoadTestReportResultRealtime record, @Param("example") LoadTestReportResultRealtimeExample example);

    int updateByPrimaryKeySelective(LoadTestReportResultRealtime record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportResultRealtime record);
}