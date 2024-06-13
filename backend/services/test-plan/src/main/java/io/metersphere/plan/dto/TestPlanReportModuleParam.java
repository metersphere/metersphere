package io.metersphere.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestPlanReportModuleParam {

	private Map<String, String> functionalModuleMap = new HashMap<>();

	private Map<String, String> apiModuleMap = new HashMap<>();

	private Map<String, String> scenarioModuleMap = new HashMap<>();
}
