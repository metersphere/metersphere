package io.metersphere.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestPlanReportDetailCaseDTO {

	private long functionCaseCount;

	private long apiCaseCount;

	private long apiScenarioCount;

	private long bugCount;

}
