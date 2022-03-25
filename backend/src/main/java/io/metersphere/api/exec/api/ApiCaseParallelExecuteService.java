package io.metersphere.api.exec.api;

import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.RunModeConfigDTO;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ApiCaseParallelExecuteService {
    @Resource
    private ApiScenarioSerialService apiScenarioSerialService;
    @Resource
    private JMeterService jMeterService;

    public void parallel(Map<String, ApiDefinitionExecResult> executeQueue, RunModeConfigDTO config, DBTestQueue executionQueue, String runMode) {
        for (String testId : executeQueue.keySet()) {
            ApiDefinitionExecResult result = executeQueue.get(testId);
            String reportId = result.getId();
            HashTree hashTree = null;
            JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testId, reportId, runMode, hashTree);
            runRequest.setPool(GenerateHashTreeUtil.isResourcePool(config.getResourcePoolId()));
            runRequest.setTestPlanReportId(executionQueue.getReportId());
            runRequest.setPoolId(config.getResourcePoolId());
            runRequest.setReportType(executionQueue.getReportType());
            runRequest.setRunType(RunModeConstants.PARALLEL.toString());
            runRequest.setQueueId(executionQueue.getId());
            if (executionQueue.getQueue() != null) {
                runRequest.setPlatformUrl(executionQueue.getQueue().getId());
            }
            if (!GenerateHashTreeUtil.isResourcePool(config.getResourcePoolId()).isPool()) {
                hashTree = apiScenarioSerialService.generateHashTree(testId, config.getEnvMap(), runRequest);
                runRequest.setHashTree(hashTree);
            }
            jMeterService.run(runRequest);
        }
    }
}
