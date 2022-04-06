package io.metersphere.api.exec.api;

import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.TestResource;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.vo.BooleanPool;
import org.apache.commons.collections4.MapUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ApiCaseParallelExecuteService {
    @Resource
    private ApiScenarioSerialService apiScenarioSerialService;
    @Resource
    private JMeterService jMeterService;

    public void parallel(Map<String, ApiDefinitionExecResult> executeQueue, RunModeConfigDTO config, DBTestQueue executionQueue, String runMode) {
        List<TestResource> resources = new ArrayList<>();
        BooleanPool pool = GenerateHashTreeUtil.isResourcePool(config.getResourcePoolId());
        if (pool.isPool()) {
            resources = GenerateHashTreeUtil.setPoolResource(config.getResourcePoolId());
        }

        for (String testId : executeQueue.keySet()) {
            ApiDefinitionExecResult result = executeQueue.get(testId);
            String reportId = result.getId();
            JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testId, reportId, runMode, null);
            runRequest.setPool(GenerateHashTreeUtil.isResourcePool(config.getResourcePoolId()));
            runRequest.setTestPlanReportId(executionQueue.getReportId());
            runRequest.setPoolId(config.getResourcePoolId());
            runRequest.setReportType(executionQueue.getReportType());
            runRequest.setRunType(RunModeConstants.PARALLEL.toString());
            runRequest.setQueueId(executionQueue.getId());
            if (MapUtils.isNotEmpty(executionQueue.getDetailMap())) {
                runRequest.setPlatformUrl(executionQueue.getDetailMap().get(result.getId()));
            }
            if (!pool.isPool()) {
                HashTree hashTree = apiScenarioSerialService.generateHashTree(testId, config.getEnvMap(), runRequest);
                runRequest.setHashTree(hashTree);
            }
            jMeterService.run(runRequest, resources);
        }
    }
}
