package io.metersphere.base.mapper;

import io.metersphere.base.domain.FucTestReport;
import io.metersphere.base.domain.FucTestReportExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FucTestReportMapper {
    long countByExample(FucTestReportExample example);

    int deleteByExample(FucTestReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(FucTestReport record);

    int insertSelective(FucTestReport record);

    List<FucTestReport> selectByExampleWithBLOBs(FucTestReportExample example);

    List<FucTestReport> selectByExample(FucTestReportExample example);

    FucTestReport selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FucTestReport record, @Param("example") FucTestReportExample example);

    int updateByExampleWithBLOBs(@Param("record") FucTestReport record, @Param("example") FucTestReportExample example);

    int updateByExample(@Param("record") FucTestReport record, @Param("example") FucTestReportExample example);

    int updateByPrimaryKeySelective(FucTestReport record);

    int updateByPrimaryKeyWithBLOBs(FucTestReport record);

    int updateByPrimaryKey(FucTestReport record);
}