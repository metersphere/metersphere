package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanReportContentWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanReportContentMapper {

    boolean isDynamicallyGenerateReport(@Param("reportId") String reportId);

    TestPlanReportContentWithBLOBs selectForPassRate(@Param("reportId") String reportId);

    boolean hasRunningReport(@Param("planId") String planId);

    boolean hasRunningReportByPlanIds(@Param("planIds") List<String> planIds);

    boolean isApiBasicCountIsNull(String testPlanReportId);
}
