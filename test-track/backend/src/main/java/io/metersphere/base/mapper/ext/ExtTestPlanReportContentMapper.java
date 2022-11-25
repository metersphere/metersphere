package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanReportContentWithBLOBs;
import org.apache.ibatis.annotations.Param;

public interface ExtTestPlanReportContentMapper {

    boolean isDynamicallyGenerateReport(@Param("reportId") String reportId);

    TestPlanReportContentWithBLOBs selectForPassRate(@Param("reportId") String reportId);
}
