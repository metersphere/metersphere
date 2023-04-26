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
    
    List<String> selectScenarioReportByTestPlanReportIds(@Param("testPlanReportIds") List<String> testPlanReportIdList);

    List<String> selectUiReportByTestPlanReportIds(@Param("testPlanReportIds") List<String> testPlanReportIdList);

    int deleteApiReportByTestPlanReportList(@Param("testPlanReportIds") List<String> testPlanReportIdList);

    void deleteScenarioReportByIds(@Param("testPlanReportIds") List<String> scenarioReportIds);

    void deleteScenarioReportResultByIds(@Param("testPlanReportIds") List<String> scenarioReportIds);

    void deleteScenarioReportStructureByIds(@Param("testPlanReportIds") List<String> scenarioReportIds);

    void deleteUiReportByIds(@Param("testPlanReportIds") List<String> scenarioReportIds);

    void deleteUiReportResultByIds(@Param("testPlanReportIds") List<String> scenarioReportIds);

    void deleteUiReportStructureByIds(@Param("testPlanReportIds") List<String> scenarioReportIds);

}
