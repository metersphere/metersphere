package io.metersphere.base.mapper;

import io.metersphere.base.domain.EnterpriseTestReport;
import io.metersphere.base.domain.EnterpriseTestReportExample;
import io.metersphere.base.domain.EnterpriseTestReportWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EnterpriseTestReportMapper {
    long countByExample(EnterpriseTestReportExample example);

    int deleteByExample(EnterpriseTestReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(EnterpriseTestReportWithBLOBs record);

    int insertSelective(EnterpriseTestReportWithBLOBs record);

    List<EnterpriseTestReportWithBLOBs> selectByExampleWithBLOBs(EnterpriseTestReportExample example);

    List<EnterpriseTestReport> selectByExample(EnterpriseTestReportExample example);

    EnterpriseTestReportWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") EnterpriseTestReportWithBLOBs record, @Param("example") EnterpriseTestReportExample example);

    int updateByExampleWithBLOBs(@Param("record") EnterpriseTestReportWithBLOBs record, @Param("example") EnterpriseTestReportExample example);

    int updateByExample(@Param("record") EnterpriseTestReport record, @Param("example") EnterpriseTestReportExample example);

    int updateByPrimaryKeySelective(EnterpriseTestReportWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(EnterpriseTestReportWithBLOBs record);

    int updateByPrimaryKey(EnterpriseTestReport record);
}