package io.metersphere.plan.service.remote.performance;

import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.plan.request.performance.PlanPerformanceExecRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PerfExecService extends LoadTestService {

    private static final String BASE_UEL = "/performance/exec";

    public Map<String, String> run(PlanPerformanceExecRequest request) {
       return (Map<String, String>) microService.postForData(serviceName, BASE_UEL + "/run", request);
    }

    public Map<String, String> executeLoadCase(String planReportId, RunModeConfigDTO runModeConfig, String triggerMode, Map<String, String> performanceIdMap) {
        PlanPerformanceExecRequest request = new PlanPerformanceExecRequest();
        request.setPlanReportId(planReportId);
        request.setConfig(runModeConfig);
        request.setTriggerMode(triggerMode);
        request.setPerfMap(performanceIdMap);
        request.getConfig().setMode(RunModeConstants.PARALLEL.toString());
        return this.run(request);
    }
}
