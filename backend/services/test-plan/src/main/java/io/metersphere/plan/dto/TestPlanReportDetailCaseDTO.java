package io.metersphere.plan.dto;

import io.metersphere.plan.domain.TestPlanReportApiCase;
import io.metersphere.plan.domain.TestPlanReportApiScenario;
import io.metersphere.plan.domain.TestPlanReportBug;
import io.metersphere.plan.domain.TestPlanReportFunctionCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestPlanReportDetailCaseDTO {

	private List<TestPlanReportFunctionCase> functionCases;

	private List<TestPlanReportApiCase> apiCases;

	private List<TestPlanReportApiScenario> apiScenarios;

	private List<TestPlanReportBug> bugs;

}
