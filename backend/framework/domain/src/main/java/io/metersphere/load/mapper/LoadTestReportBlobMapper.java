package io.metersphere.load.mapper;

import io.metersphere.load.domain.LoadTestReportBlob;
import io.metersphere.load.domain.LoadTestReportBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestReportBlobMapper {
    long countByExample(LoadTestReportBlobExample example);

    int deleteByExample(LoadTestReportBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(LoadTestReportBlob record);

    int insertSelective(LoadTestReportBlob record);

    List<LoadTestReportBlob> selectByExampleWithBLOBs(LoadTestReportBlobExample example);

    List<LoadTestReportBlob> selectByExample(LoadTestReportBlobExample example);

    LoadTestReportBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LoadTestReportBlob record, @Param("example") LoadTestReportBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportBlob record, @Param("example") LoadTestReportBlobExample example);

    int updateByExample(@Param("record") LoadTestReportBlob record, @Param("example") LoadTestReportBlobExample example);

    int updateByPrimaryKeySelective(LoadTestReportBlob record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportBlob record);
}