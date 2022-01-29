package io.metersphere.api.exec.scenario;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ApiScenarioParallelService {
    @Resource
    private JMeterService jMeterService;

    public void parallel(Map<String, RunModeDataDTO> executeQueue, RunScenarioRequest request, String serialReportId, DBTestQueue executionQueue) {
        for (String reportId : executeQueue.keySet()) {
            RunModeDataDTO dataDTO = executeQueue.get(reportId);
            JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(dataDTO.getTestId(), StringUtils.isNotEmpty(serialReportId) ? serialReportId : reportId, request.getRunMode(), null);
            runRequest.setReportType(StringUtils.isNotEmpty(serialReportId) ? RunModeConstants.SET_REPORT.toString() : RunModeConstants.INDEPENDENCE.toString());
            runRequest.setQueueId(executionQueue.getId());
            if (request.getConfig() != null) {
                runRequest.setPool(GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId()));
                runRequest.setPoolId(request.getConfig().getResourcePoolId());
            }
            runRequest.setTestPlanReportId(request.getTestPlanReportId());
            runRequest.setPlatformUrl(executionQueue.getDetailMap().get(reportId));
            runRequest.setRunType(RunModeConstants.PARALLEL.toString());
            if (LoggerUtil.getLogger().isDebugEnabled()) {
                LoggerUtil.debug("Scenario run-开始并发执行：" + JSON.toJSONString(request));
            }
            // 本地执行生成hashTree
            if (request.getConfig() != null && !runRequest.getPool().isPool()) {
                runRequest.setHashTree(GenerateHashTreeUtil.generateHashTree(dataDTO.getScenario(), dataDTO.getPlanEnvMap(), runRequest));
            }
            jMeterService.run(runRequest);
        }
    }
}
