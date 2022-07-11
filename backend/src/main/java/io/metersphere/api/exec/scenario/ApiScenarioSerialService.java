package io.metersphere.api.exec.scenario;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.exec.utils.RequestParamsUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.utils.SmoothWeighted;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.HashTreeUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioSerialService {
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiScenarioEnvService apiScenarioEnvService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ApiTestEnvironmentService apiTestEnvironmentService;

    public void serial(DBTestQueue executionQueue) {
        ApiExecutionQueueDetail queue = executionQueue.getDetail();
        String reportId = StringUtils.isNotEmpty(executionQueue.getReportId()) ? executionQueue.getReportId() : queue.getReportId();
        if (!StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name())) {
            reportId = queue.getReportId();
        }
        JmeterRunRequestDTO runRequest = RequestParamsUtil.init(executionQueue, queue, reportId);
        // 更新报告状态
        updateReportToRunning(queue, runRequest);
        try {
            if (StringUtils.isEmpty(executionQueue.getPoolId())) {
                if (StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                    ApiScenarioWithBLOBs scenario = null;
                    Map<String, String> planEnvMap = new LinkedHashMap<>();
                    if (StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCHEDULE_SCENARIO.name())) {
                        scenario = apiScenarioMapper.selectByPrimaryKey(queue.getTestId());
                    } else {
                        TestPlanApiScenario planApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(queue.getTestId());
                        if (planApiScenario != null) {
                            planEnvMap = apiScenarioEnvService.planEnvMap(queue.getTestId());
                            queue.setEvnMap(JSON.toJSONString(planEnvMap));
                            scenario = apiScenarioMapper.selectByPrimaryKey(planApiScenario.getApiScenarioId());
                        }
                    }
                    if ((planEnvMap == null || planEnvMap.isEmpty()) && StringUtils.isNotEmpty(queue.getEvnMap())) {
                        planEnvMap = JSON.parseObject(queue.getEvnMap(), Map.class);
                    }
                    runRequest.setHashTree(GenerateHashTreeUtil.generateHashTree(scenario, planEnvMap, runRequest));
                }

                // 更新环境变量
                if (runRequest.getHashTree() != null) {
                    this.initEnv(runRequest.getHashTree());
                }
            }
            if (runRequest.getPool().isPool()) {
                SmoothWeighted.setServerConfig(runRequest.getPoolId(), redisTemplate);
            }
            // 开始执行
            jMeterService.run(runRequest);
        } catch (Exception e) {
            RequestParamsUtil.rollback(runRequest, e);
        }
    }

    /**
     * 更新报告状态
     *
     * @param queue
     * @param runRequest
     */
    public void updateReportToRunning(ApiExecutionQueueDetail queue, JmeterRunRequestDTO runRequest) {
        if (!GenerateHashTreeUtil.isSetReport(runRequest.getReportType()) &&
                StringUtils.equalsAny(runRequest.getRunMode(),
                        ApiRunMode.SCENARIO.name(),
                        ApiRunMode.SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO.name(),
                        ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(queue.getReportId());
            if (report != null) {
                report.setStatus(APITestStatus.Running.name());
                report.setCreateTime(System.currentTimeMillis());
                report.setUpdateTime(System.currentTimeMillis());
                runRequest.setExtendedParameters(new HashMap<String, Object>() {{
                    this.put("userId", report.getCreateUser());
                }});
                apiScenarioReportMapper.updateByPrimaryKey(report);
                LoggerUtil.info("进入串行模式，准备执行资源：[ " + report.getName() + " ]", report.getId());
            }
        }
    }

    private void initEnv(HashTree hashTree) {
        HashTreeUtil hashTreeUtil = new HashTreeUtil();
        Map<String, Map<String, String>> envParamsMap = hashTreeUtil.getEnvParamsDataByHashTree(hashTree, apiTestEnvironmentService);
        hashTreeUtil.mergeParamDataMap(null, envParamsMap);
    }
}
