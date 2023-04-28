package io.metersphere.api.exec.scenario;

import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.utils.SmoothWeighted;
import io.metersphere.api.service.RemakeReportService;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.service.SystemParameterService;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.vo.BooleanPool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioParallelService {
    @Resource
    protected JMeterService jMeterService;
    @Resource
    protected SystemParameterService systemParameterService;
    @Resource
    protected RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RemakeReportService remakeReportService;

    public void parallel(Map<String, RunModeDataDTO> executeQueue, RunScenarioRequest request, String serialReportId, DBTestQueue executionQueue) {
        // 初始化分配策略
        BooleanPool pool = GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId());
        if (pool.isPool()) {
            SmoothWeighted.setServerConfig(request.getConfig().getResourcePoolId(), redisTemplate);
        }
        // 获取可以执行的资源池
        BaseSystemConfigDTO baseInfo = systemParameterService.getBaseInfo();
        for (String reportId : executeQueue.keySet()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            RunModeDataDTO dataDTO = executeQueue.get(reportId);
            JmeterRunRequestDTO runRequest = getJmeterRunRequestDTO(request, serialReportId, executionQueue, baseInfo, reportId, dataDTO);
            try {
                runRequest.setPool(pool);
                runRequest.setPoolId(request.getConfig().getResourcePoolId());
                // 本地执行生成hashTree
                if (!pool.isPool()) {
                    runRequest.setHashTree(
                            GenerateHashTreeUtil.generateHashTree(
                                    dataDTO.getScenario(), dataDTO.getPlanEnvMap(), runRequest));
                }
            } catch (Exception ex) {
                remakeReportService.testEnded(runRequest, ex.getMessage());
                continue;
            }
            LoggerUtil.info("进入并行模式，准备执行场景：[ "
                    + executeQueue.get(reportId).getReport().getName() + " ]", reportId);
            jMeterService.run(runRequest);
        }
    }

    protected JmeterRunRequestDTO getJmeterRunRequestDTO(RunScenarioRequest request, String serialReportId, DBTestQueue executionQueue,
                                                         BaseSystemConfigDTO baseInfo, String reportId, RunModeDataDTO dataDTO) {
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(dataDTO.getTestId(), StringUtils.isNotEmpty(serialReportId) ? serialReportId : reportId, request.getRunMode(), null);
        runRequest.setReportType(StringUtils.isNotEmpty(serialReportId) ? RunModeConstants.SET_REPORT.toString() : RunModeConstants.INDEPENDENCE.toString());
        runRequest.setQueueId(executionQueue.getId());
        runRequest.setTestPlanReportId(request.getTestPlanReportId());
        runRequest.setPlatformUrl(GenerateHashTreeUtil.getPlatformUrl(baseInfo, runRequest, executionQueue.getDetailMap().get(reportId)));
        runRequest.setRunType(RunModeConstants.PARALLEL.toString());
        return runRequest;
    }
}
