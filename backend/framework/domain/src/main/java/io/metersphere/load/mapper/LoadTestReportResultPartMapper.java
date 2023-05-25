package io.metersphere.load.mapper;

import io.metersphere.load.domain.LoadTestReportResultPart;
import io.metersphere.load.domain.LoadTestReportResultPartExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestReportResultPartMapper {
    long countByExample(LoadTestReportResultPartExample example);

    int deleteByExample(LoadTestReportResultPartExample example);

    int deleteByPrimaryKey(@Param("reportId") String reportId, @Param("reportKey") String reportKey, @Param("resourceIndex") Integer resourceIndex);

    int insert(LoadTestReportResultPart record);

    int insertSelective(LoadTestReportResultPart record);

    List<LoadTestReportResultPart> selectByExampleWithBLOBs(LoadTestReportResultPartExample example);

    List<LoadTestReportResultPart> selectByExample(LoadTestReportResultPartExample example);

    LoadTestReportResultPart selectByPrimaryKey(@Param("reportId") String reportId, @Param("reportKey") String reportKey, @Param("resourceIndex") Integer resourceIndex);

    int updateByExampleSelective(@Param("record") LoadTestReportResultPart record, @Param("example") LoadTestReportResultPartExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportResultPart record, @Param("example") LoadTestReportResultPartExample example);

    int updateByExample(@Param("record") LoadTestReportResultPart record, @Param("example") LoadTestReportResultPartExample example);

    int updateByPrimaryKeySelective(LoadTestReportResultPart record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportResultPart record);
}