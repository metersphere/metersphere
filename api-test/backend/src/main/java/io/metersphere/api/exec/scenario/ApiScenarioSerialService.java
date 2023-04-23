package io.metersphere.api.exec.scenario;


import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.utils.SmoothWeighted;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.plan.TestPlanApiScenarioMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.GenerateHashTreeUtil;
import io.metersphere.commons.utils.HashTreeUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.RequestParamsUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.service.RemakeReportService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Lazy
    @Resource
    private ApiScenarioEnvService apiScenarioEnvService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private BaseEnvironmentService apiTestEnvironmentService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private RemakeReportService remakeReportService;

    public void serial(DBTestQueue executionQueue) {
        ApiExecutionQueueDetail queue = executionQueue.getDetail();
        String reportId = StringUtils.isNotEmpty(executionQueue.getReportId()) ? executionQueue.getReportId() : queue.getReportId();
        if (!StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name())) {
            reportId = queue.getReportId();
        }
        JmeterRunRequestDTO runRequest = RequestParamsUtil.init(executionQueue, queue, reportId);
        runRequest.setRunType(RunModeConstants.SERIAL.toString());
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
                            // envMap不为空的话可以看做是需要指定环境运行； 为空的话则按照默认环境运行
                            if (MapUtils.isEmpty(JSON.parseObject(queue.getEvnMap(), Map.class))) {
                                planEnvMap = apiScenarioEnvService.planEnvMap(queue.getTestId());
                                queue.setEvnMap(JSON.toJSONString(planEnvMap));
                            }
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
            runRequest.getExtendedParameters().put("projectId", queue.getProjectIds());
        } catch (Exception e) {
            remakeReportService.testEnded(runRequest, e.getMessage());
            LoggerUtil.error("脚本处理失败", runRequest.getReportId(), e);
            return;
        }
        // 更新报告状态
        updateReportToRunning(queue, runRequest);
        jMeterService.run(runRequest);
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
                        ApiRunMode.JENKINS_SCENARIO_PLAN.name(),
                        ApiRunMode.UI_SCENARIO.name(),
                        ApiRunMode.UI_SCENARIO_PLAN.name(),
                        ApiRunMode.UI_JENKINS_SCENARIO_PLAN.name(),
                        ApiRunMode.UI_SCHEDULE_SCENARIO.name(),
                        ApiRunMode.UI_SCHEDULE_SCENARIO_PLAN.name())
        ) {
            ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(queue.getReportId());
            if (report != null) {
                report.setStatus(ApiReportStatus.RUNNING.name());
                report.setCreateTime(System.currentTimeMillis());
                report.setUpdateTime(System.currentTimeMillis());
                runRequest.setExtendedParameters(new HashMap<>() {{
                    this.put(CommonConstants.USER_ID, report.getCreateUser());
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
