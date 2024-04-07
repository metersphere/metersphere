package io.metersphere.api.job;

import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.dto.ApiScenarioParamConfig;
import io.metersphere.api.dto.ApiScenarioParseTmpParam;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.dto.scenario.ApiScenarioParseParam;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.ApiExecuteRunMode;
import io.metersphere.sdk.constants.TaskTriggerMode;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.schedule.BaseScheduleJob;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.lang3.StringUtils;

import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class ApiScenarioScheduleJob extends BaseScheduleJob {
    @Override
    protected void businessExecute(JobExecutionContext context) {
        ApiScenarioService apiScenarioService = CommonBeanFactory.getBean(ApiScenarioService.class);
        ApiExecuteService apiExecuteService = CommonBeanFactory.getBean(ApiExecuteService.class);
        ApiRunModeConfigDTO apiRunModeConfigDTO = JSON.parseObject(JSON.toJSONString(context.getJobDetail().getJobDataMap()), ApiRunModeConfigDTO.class);

        ApiScenarioDetail apiScenarioDetail = apiScenarioService.getForRun(resourceId);
        if (apiScenarioDetail == null) {
            LogUtils.info("当前定时任务的场景已删除 {}", resourceId);
            return;
        }
        MsScenario msScenario = apiScenarioService.getMsScenario(apiScenarioDetail);
        ApiScenarioParseParam parseParam = apiScenarioService.getApiScenarioParseParam(apiScenarioDetail);
        parseParam.setEnvironmentId(apiRunModeConfigDTO.getEnvironmentId());
        parseParam.setGrouped(apiRunModeConfigDTO.getGrouped());

        if (StringUtils.isBlank(apiRunModeConfigDTO.getPoolId())) {
            apiRunModeConfigDTO.setPoolId(apiExecuteService.getProjectApiResourcePoolId(apiScenarioDetail.getProjectId()));
        }

        // 解析生成场景树，并保存临时变量
        ApiScenarioParseTmpParam tmpParam = apiScenarioService.parse(msScenario, apiScenarioDetail.getSteps(), parseParam);

        ApiResourceRunRequest runRequest = apiScenarioService.getApiResourceRunRequest(msScenario, tmpParam);

        TaskRequestDTO taskRequest = apiScenarioService.getTaskRequest(IDGenerator.nextStr(), apiScenarioDetail.getId(), apiScenarioDetail.getProjectId(), ApiExecuteRunMode.SCENARIO.name());
        taskRequest.getRunModeConfig().setPoolId(apiRunModeConfigDTO.getPoolId());
        taskRequest.setSaveResult(true);
        taskRequest.setRealTime(false);
        taskRequest.getRunModeConfig().setEnvironmentId(parseParam.getEnvironmentId());
        taskRequest.setRequestCount(tmpParam.getRequestCount().get());

        ApiScenarioParamConfig parseConfig = apiScenarioService.getApiScenarioParamConfig(parseParam, tmpParam.getScenarioParseEnvInfo());
        parseConfig.setReportId(taskRequest.getReportId());

        // 初始化报告
        ApiScenarioReport scenarioReport = apiScenarioService.getScenarioReport(userId);
        scenarioReport.setId(taskRequest.getReportId());
        scenarioReport.setTriggerMode(TaskTriggerMode.SCHEDULE.name());
        scenarioReport.setRunMode(ApiBatchRunMode.PARALLEL.name());
        scenarioReport.setPoolId(apiRunModeConfigDTO.getPoolId());
        scenarioReport.setEnvironmentId(parseParam.getEnvironmentId());
        apiScenarioService.initApiReport(apiScenarioDetail, scenarioReport);

        // 初始化报告步骤
        apiScenarioService.initScenarioReportSteps(apiScenarioDetail.getSteps(), taskRequest.getReportId());

        apiExecuteService.execute(runRequest, taskRequest, parseConfig);
    }

    public static JobKey getJobKey(String scenarioId) {
        return new JobKey(scenarioId, ApiScenarioScheduleJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String scenarioId) {
        return new TriggerKey(scenarioId, ApiScenarioScheduleJob.class.getName());
    }
}
