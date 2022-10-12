package io.metersphere.api.exec.api;

import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.commons.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.utils.SmoothWeighted;
import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.service.SystemParameterService;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.vo.BooleanPool;
import org.apache.commons.collections4.MapUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApiCaseParallelExecuteService {
    @Resource
    private ApiCaseSerialService apiCaseSerialService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void parallel(Map<String, ApiDefinitionExecResultWithBLOBs> executeQueue, RunModeConfigDTO config, DBTestQueue executionQueue, String runMode) {
        BooleanPool pool = GenerateHashTreeUtil.isResourcePool(config.getResourcePoolId());
        // 初始化分配策略
        if (pool.isPool()) {
            SmoothWeighted.setServerConfig(config.getResourcePoolId(), redisTemplate);
        }
        // 获取可以执行的资源池
        BaseSystemConfigDTO baseInfo = CommonBeanFactory.getBean(SystemParameterService.class).getBaseInfo();
        for (String testId : executeQueue.keySet()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            ApiDefinitionExecResultWithBLOBs result = executeQueue.get(testId);
            String reportId = result.getId();
            JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testId, reportId, runMode, null);
            runRequest.setPool(pool);
            runRequest.setTestPlanReportId(executionQueue.getReportId());
            runRequest.setPoolId(config.getResourcePoolId());
            runRequest.setReportType(executionQueue.getReportType());
            runRequest.setRunType(RunModeConstants.PARALLEL.toString());
            runRequest.setQueueId(executionQueue.getId());

            runRequest.setRetryNum(config.getRetryNum());
            runRequest.setRetryEnable(config.isRetryEnable());

            Map<String, Object> extendedParameters = new HashMap<>();
            extendedParameters.put("userId", result.getUserId());
            runRequest.setExtendedParameters(extendedParameters);
            if (MapUtils.isNotEmpty(executionQueue.getDetailMap())) {
                runRequest.setPlatformUrl(GenerateHashTreeUtil.getPlatformUrl(baseInfo, runRequest, executionQueue.getDetailMap().get(result.getId())));
            }
            if (!pool.isPool()) {
                HashTree hashTree = apiCaseSerialService.generateHashTree(testId, config.getEnvMap(), runRequest);
                runRequest.setHashTree(hashTree);
            }
            // 开始执行
            runRequest.getExtendedParameters().put("projectId", executionQueue.getDetail().getProjectIds());

            LoggerUtil.info("进入并行模式，开始执行用例：[" + result.getName() + "] 报告ID [" + reportId + "]");
            jMeterService.run(runRequest);
        }
    }
}
