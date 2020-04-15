package io.metersphere.base.mapper;

import io.metersphere.base.domain.LoadTestReportDetail;
import io.metersphere.base.domain.LoadTestReportDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoadTestReportDetailMapper {
    long countByExample(LoadTestReportDetailExample example);

    int deleteByExample(LoadTestReportDetailExample example);

    int deleteByPrimaryKey(String reportId);

    int insert(LoadTestReportDetail record);

    int insertSelective(LoadTestReportDetail record);

    List<LoadTestReportDetail> selectByExampleWithBLOBs(LoadTestReportDetailExample example);

    List<LoadTestReportDetail> selectByExample(LoadTestReportDetailExample example);

    LoadTestReportDetail selectByPrimaryKey(String reportId);

    int updateByExampleSelective(@Param("record") LoadTestReportDetail record, @Param("example") LoadTestReportDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportDetail record, @Param("example") LoadTestReportDetailExample example);

    int updateByExample(@Param("record") LoadTestReportDetail record, @Param("example") LoadTestReportDetailExample example);

    int updateByPrimaryKeySelective(LoadTestReportDetail record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportDetail record);
}