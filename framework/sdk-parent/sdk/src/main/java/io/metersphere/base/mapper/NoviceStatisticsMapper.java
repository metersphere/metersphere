package io.metersphere.base.mapper;

import io.metersphere.base.domain.NoviceStatistics;
import io.metersphere.base.domain.NoviceStatisticsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NoviceStatisticsMapper {
    long countByExample(NoviceStatisticsExample example);

    int deleteByExample(NoviceStatisticsExample example);

    int deleteByPrimaryKey(String id);

    int insert(NoviceStatistics record);

    int insertSelective(NoviceStatistics record);

    List<NoviceStatistics> selectByExampleWithBLOBs(NoviceStatisticsExample example);

    List<NoviceStatistics> selectByExample(NoviceStatisticsExample example);

    NoviceStatistics selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") NoviceStatistics record, @Param("example") NoviceStatisticsExample example);

    int updateByExampleWithBLOBs(@Param("record") NoviceStatistics record, @Param("example") NoviceStatisticsExample example);

    int updateByExample(@Param("record") NoviceStatistics record, @Param("example") NoviceStatisticsExample example);

    int updateByPrimaryKeySelective(NoviceStatistics record);

    int updateByPrimaryKeyWithBLOBs(NoviceStatistics record);

    int updateByPrimaryKey(NoviceStatistics record);
}