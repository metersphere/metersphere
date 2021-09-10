package io.metersphere.base.mapper;

import io.metersphere.base.domain.ReportStatistics;
import io.metersphere.base.domain.ReportStatisticsExample;
import io.metersphere.base.domain.ReportStatisticsWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReportStatisticsMapper {
    long countByExample(ReportStatisticsExample example);

    int deleteByExample(ReportStatisticsExample example);

    int deleteByPrimaryKey(String id);

    int insert(ReportStatisticsWithBLOBs record);

    int insertSelective(ReportStatisticsWithBLOBs record);

    List<ReportStatisticsWithBLOBs> selectByExampleWithBLOBs(ReportStatisticsExample example);

    List<ReportStatistics> selectByExample(ReportStatisticsExample example);

    ReportStatisticsWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ReportStatisticsWithBLOBs record, @Param("example") ReportStatisticsExample example);

    int updateByExampleWithBLOBs(@Param("record") ReportStatisticsWithBLOBs record, @Param("example") ReportStatisticsExample example);

    int updateByExample(@Param("record") ReportStatistics record, @Param("example") ReportStatisticsExample example);

    int updateByPrimaryKeySelective(ReportStatisticsWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ReportStatisticsWithBLOBs record);

    int updateByPrimaryKey(ReportStatistics record);
}