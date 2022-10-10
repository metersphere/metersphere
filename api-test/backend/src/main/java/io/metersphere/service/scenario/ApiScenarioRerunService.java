package io.metersphere.service.scenario;

import io.metersphere.api.dto.ApiCaseRunRequest;
import io.metersphere.api.dto.ApiScenarioReportDTO;
import io.metersphere.api.dto.StepTreeDTO;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.exec.api.ApiCaseExecuteService;
import io.metersphere.api.exec.scenario.ApiScenarioExecuteService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportResultMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.RerunParametersDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApiScenarioRerunService {
    @Resource
    private ExtApiDefinitionExecResultMapper extDefinitionExecResultMapper;
    @Resource
    private ApiCaseExecuteService apiCaseExecuteService;
    @Resource
    private ApiScenarioReportStructureService apiScenarioReportStructureService;
    @Resource
    private ApiScenarioExecuteService apiScenarioExecuteService;
    @Resource
    private ExtApiScenarioReportResultMapper extApiScenarioReportResultMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    /**
     * 失败重跑
     */
    public String rerun(RerunParametersDTO parametersDTO) {
        if (parametersDTO == null || StringUtils.isEmpty(parametersDTO.getType()) || StringUtils.isEmpty(parametersDTO.getReportId())) {
            return Translator.get("report_warning");
        }
        if (StringUtils.equalsAnyIgnoreCase(parametersDTO.getType(), ReportTypeConstants.API_INTEGRATED.name())) {
            this.apiRerun(parametersDTO.getReportId());
        } else if (StringUtils.equalsAnyIgnoreCase(parametersDTO.getType(), ReportTypeConstants.SCENARIO_INTEGRATED.name())) {
            this.scenarioReRun(parametersDTO.getReportId());
        } else {
            return Translator.get("report_type_error");
        }
        return "SUCCESS";
    }


    /**
     * CASE 集合报告重跑
     *
     * @param reportId
     */
    private void apiRerun(String reportId) {
        List<ApiDefinitionExecResultWithBLOBs> reportResults = extDefinitionExecResultMapper.selectRerunResult(reportId);
        LoggerUtil.info("获取到重跑数据【" + reportId + "】" + reportResults.size());

        if (CollectionUtils.isNotEmpty(reportResults)) {
            List<String> resourceIds = reportResults.stream().map(ApiDefinitionExecResult::getResourceId).collect(Collectors.toList());
            // 执行资源
            Map<String, ApiDefinitionExecResultWithBLOBs> map = reportResults.stream().collect(Collectors.toMap(ApiDefinitionExecResult::getResourceId, api -> api));

            ApiCaseRunRequest request = new ApiCaseRunRequest();
            request.setRerun(true);
            request.setExecuteQueue(map);
            request.setIds(resourceIds);
            request.setReportId(reportId);

            ApiDefinitionExecResultWithBLOBs resultWithBLOBs = reportResults.get(0);
            // 执行配置
            RunModeConfigDTO config = StringUtils.isNotEmpty(resultWithBLOBs.getEnvConfig()) ? JSON.parseObject(resultWithBLOBs.getEnvConfig(), RunModeConfigDTO.class) : new RunModeConfigDTO();
            if (StringUtils.isEmpty(config.getMode())) {
                config.setMode(RunModeConstants.PARALLEL.toString());
            }
            config.setReportType(RunModeConstants.SET_REPORT.toString());
            if (!StringUtils.equalsAnyIgnoreCase(reportResults.get(0).getActuator(), "LOCAL")) {
                config.setResourcePoolId(reportResults.get(0).getActuator());
            }
            request.setConfig(config);

            LoggerUtil.info("开始重跑执行【" + reportId + "】");
            apiCaseExecuteService.run(request);
        }

    }

    private void sortById(List<String> ids, List apiScenarios) {
        FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(ids);
        fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
        BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
        Collections.sort(apiScenarios, beanComparator);
    }

    /**
     * 场景集合报告失败重跑
     */
    private void scenarioReRun(String reportId) {
        ApiScenarioReportDTO reportDTO = apiScenarioReportStructureService.assembleReport(reportId, false);
        if (reportDTO != null && CollectionUtils.isNotEmpty(reportDTO.getSteps())) {
            List<StepTreeDTO> rerunSteps = reportDTO.getSteps().stream().filter(step -> StringUtils.equalsAnyIgnoreCase(step.getType(), ElementConstants.SCENARIO) && (StringUtils.equalsAnyIgnoreCase(step.getTotalStatus(), ApiReportStatus.ERROR.name()) || step.getUnExecuteTotal() > 0)).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(rerunSteps)) {
                List<String> ids = rerunSteps.stream().map(StepTreeDTO::getStepId).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(ids)) {
                    return;
                }
                // 检查资源是否存在
                ApiScenarioExample scenarioExample = new ApiScenarioExample();
                scenarioExample.createCriteria().andIdIn(ids).andStatusNotEqualTo("Trash");
                List<ApiScenario> scenarios = apiScenarioMapper.selectByExample(scenarioExample);
                if (CollectionUtils.isEmpty(scenarios)) {
                    return;
                }
                // 排序
                sortById(ids, scenarios);
                ids = scenarios.stream().map(ApiScenario::getId).collect(Collectors.toList());
                RunScenarioRequest request = new RunScenarioRequest();
                request.setId(UUID.randomUUID().toString());
                request.setRerun(true);
                request.setExecuteType(ExecuteType.Completed.name());
                request.setSerialReportId(reportId);
                request.setRunMode(ApiRunMode.SCENARIO.name());
                request.setIds(ids);
                // 执行配置
                RunModeConfigDTO config = StringUtils.isNotEmpty(reportDTO.getEnvConfig()) ? JSON.parseObject(reportDTO.getEnvConfig(), RunModeConfigDTO.class) : new RunModeConfigDTO();
                if (StringUtils.isEmpty(config.getMode())) {
                    config.setMode(RunModeConstants.PARALLEL.toString());
                }
                config.setReportName(reportDTO.getName());
                if (!StringUtils.equalsAnyIgnoreCase(reportDTO.getActuator(), "LOCAL")) {
                    config.setResourcePoolId(reportDTO.getActuator());
                }
                config.setReportType(RunModeConstants.SET_REPORT.toString());
                request.setConfig(config);
                LoggerUtil.info("开始清理重跑前结果数据【" + reportId + "】");
                extApiScenarioReportResultMapper.deleteHisReportResult(ids, reportId);

                // 存储重跑结构
                List<ApiScenarioWithBLOBs> scenarioWithBLOBs = apiScenarioExecuteService.get(request);
                // 重跑的报告结果
                List<StepTreeDTO> stepTreeDTOS = apiScenarioReportStructureService.get(scenarioWithBLOBs, request.getConfig().getReportType());

                LoggerUtil.info("开始更新重跑报告结构：【" + reportId + "】," + stepTreeDTOS.size());
                if (CollectionUtils.isNotEmpty(stepTreeDTOS) && CollectionUtils.isNotEmpty(reportDTO.getSteps())) {
                    this.marge(reportId, reportDTO.getSteps(), stepTreeDTOS);
                }

                LoggerUtil.info("获取到重跑数据【" + reportId + "】，【" + ids.size() + " 】开始重跑");
                apiScenarioExecuteService.run(request);
            }
        }
    }

    private void marge(String reportId, List<StepTreeDTO> all, List<StepTreeDTO> newSteps) {
        Map<String, StepTreeDTO> map = newSteps.stream().collect(Collectors.toMap(StepTreeDTO::getResourceId, student -> student));
        all.forEach(item -> {
            if (map.containsKey(item.getResourceId())) {
                item.setChildren(map.get(item.getResourceId()).getChildren());
            }
        });
        apiScenarioReportStructureService.update(reportId, all);
    }
}
