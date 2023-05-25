package io.metersphere.load.mapper;

import io.metersphere.load.domain.LoadTestReportFile;
import io.metersphere.load.domain.LoadTestReportFileExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestReportFileMapper {
    long countByExample(LoadTestReportFileExample example);

    int deleteByExample(LoadTestReportFileExample example);

    int insert(LoadTestReportFile record);

    int insertSelective(LoadTestReportFile record);

    List<LoadTestReportFile> selectByExample(LoadTestReportFileExample example);

    int updateByExampleSelective(@Param("record") LoadTestReportFile record, @Param("example") LoadTestReportFileExample example);

    int updateByExample(@Param("record") LoadTestReportFile record, @Param("example") LoadTestReportFileExample example);
}