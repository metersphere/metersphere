package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportApiCase;
import io.metersphere.plan.domain.TestPlanReportApiScenario;

import java.util.List;

public interface ExtTestPlanReportCaseMapper {

    List<String> selectFunctionalCaseIdsByTestPlanId(String testPlanId);

    List<TestPlanReportApiCase> selectApiCaseIdAndResultByReportId(String testPlanReportId);

    List<TestPlanReportApiScenario> selectApiScenarioIdAndResultByReportId(String testPlanReportId);
}
