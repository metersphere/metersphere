package io.metersphere.service.scenario;

import io.metersphere.api.dto.ApiCaseRunRequest;
import io.metersphere.api.dto.ApiScenarioReportDTO;
import io.metersphere.api.dto.StepTreeDTO;
import io.metersphere.api.dto.automation.ApiScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.exec.api.ApiCaseExecuteService;
import io.metersphere.api.exec.scenario.ApiScenarioExecuteService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportResultMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiScenarioMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.enums.StorageEnums;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.KeyValueDTO;
import io.metersphere.dto.RerunParametersDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.dto.TestPlanRerunParametersDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
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
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioReportResultMapper apiScenarioReportResultMapper;
    @Resource
    private ApiScenarioReportStructureMapper apiScenarioReportStructureMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;

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
            LinkedHashMap<String, ApiDefinitionExecResultWithBLOBs> map = reportResults.stream().collect(
                    Collectors.toMap(ApiDefinitionExecResultWithBLOBs::getResourceId, order -> order, (k1, k2) -> k1, LinkedHashMap::new));

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
            if (!StringUtils.equalsAnyIgnoreCase(reportResults.get(0).getActuator(), StorageEnums.LOCAL.name())) {
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
                if (!StringUtils.equalsAnyIgnoreCase(reportDTO.getActuator(), StorageEnums.LOCAL.name())) {
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

    /**
     * 测试计划失败重跑
     *
     * @param parametersDTO
     * @param parametersDTO
     */
    public boolean planRerun(TestPlanRerunParametersDTO parametersDTO) {
        // 用例
        boolean isStart = false;
        TestPlanReport testPlanReport = parametersDTO.getTestPlanReport();
        if (testPlanReport == null) {
            return false;
        }

        if (CollectionUtils.isNotEmpty(parametersDTO.getCases())) {
            RunModeConfigDTO runModeConfig = new RunModeConfigDTO();
            runModeConfig.setMode(RunModeConstants.SERIAL.name());
            runModeConfig.setReportType(RunModeConstants.INDEPENDENCE.toString());
            runModeConfig.setEnvMap(new HashMap<>());
            runModeConfig.setOnSampleError(false);
            runModeConfig.setMode(ApiRunMode.API_PLAN.name());

            List<String> ids = parametersDTO.getCases().stream().map(KeyValueDTO::getId).collect(Collectors.toList());
            LoggerUtil.info("重跑测试计划报告：【" + parametersDTO.getReportId() + "】,对应重跑用例资源：" + ids.size());

            BatchRunDefinitionRequest request = new BatchRunDefinitionRequest();
            request.setTriggerMode(testPlanReport.getTriggerMode());
            request.setRerun(true);
            request.setPlanCaseIds(ids);
            request.setPlanReportId(parametersDTO.getReportId());
            request.setUserId(parametersDTO.getCases().get(0).getUserId());

            Map<String, ApiDefinitionExecResultWithBLOBs> executeQueue = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(ids)) {
                List<TestPlanApiCase> planApiCases = extTestPlanApiCaseMapper.selectByIdsAndStatusIsNotTrash(ids);
                if (CollectionUtils.isNotEmpty(planApiCases)) {
                    for (KeyValueDTO testPlanApiCase : parametersDTO.getCases()) {
                        ApiDefinitionExecResultWithBLOBs report = apiDefinitionExecResultMapper.selectByPrimaryKey(testPlanApiCase.getReportId());
                        if (report == null) {
                            continue;
                        }
                        runModeConfig.setReportType(report.getReportType());
                        if (!StringUtils.equalsAnyIgnoreCase(report.getActuator(), StorageEnums.LOCAL.name())) {
                            runModeConfig.setResourcePoolId(report.getActuator());
                        }
                        executeQueue.put(testPlanApiCase.getId(), report);
                        if (StringUtils.isNotEmpty(report.getEnvConfig())) {
                            request.setConfig(JSON.parseObject(report.getEnvConfig(), RunModeConfigDTO.class));
                        }
                    }
                    if (request.getConfig() == null) {
                        request.setConfig(runModeConfig);
                    }
                    request.setExecuteQueue(executeQueue);
                    apiCaseExecuteService.run(request);
                    isStart = true;
                }
            }
        }
        // 场景
        if (CollectionUtils.isNotEmpty(parametersDTO.getScenarios())) {
            Map<String, String> planScenarioReportMap = new LinkedHashMap<>();
            parametersDTO.getScenarios().forEach(item -> {
                if (StringUtils.isNoneBlank(item.getId(), item.getReportId())) {
                    planScenarioReportMap.put(item.getId(), item.getReportId());
                }
            });
            if (MapUtils.isEmpty(planScenarioReportMap)) {
                return isStart;
            }
            List<String> paramPlanScenarioIds = new ArrayList<>(planScenarioReportMap.keySet());
            List<TestPlanApiScenario> apiScenarios = extTestPlanApiScenarioMapper.selectByIdsAndStatusIsNotTrash(paramPlanScenarioIds);

            if (CollectionUtils.isEmpty(apiScenarios)) {
                return isStart;
            }
            List<String> scenarioIds = apiScenarios.stream().map(TestPlanApiScenario::getApiScenarioId).collect(Collectors.toList());
            LoggerUtil.info("重跑测试计划报告：【" + parametersDTO.getReportId() + "】,对应重跑场景资源：" + scenarioIds.size());
            List<String> planScenarioIds = apiScenarios.stream().map(TestPlanApiScenario::getId).collect(Collectors.toList());
            // 查出原始报告
            List<String> reportIds = new ArrayList<>();
            apiScenarios.forEach(item -> {
                if (planScenarioReportMap.containsKey(item.getId())) {
                    reportIds.add(planScenarioReportMap.get(item.getId()));
                }
            });

            if (CollectionUtils.isEmpty(reportIds)) {
                return isStart;
            }
            ApiScenarioReportExample reportExample = new ApiScenarioReportExample();
            reportExample.createCriteria().andIdIn(reportIds);
            List<ApiScenarioReport> reports = apiScenarioReportMapper.selectByExample(reportExample);
            if (CollectionUtils.isEmpty(reports)) {
                return isStart;
            }
            // config
            RunModeConfigDTO runModeConfig = new RunModeConfigDTO();
            runModeConfig.setMode(RunModeConstants.SERIAL.name());
            runModeConfig.setReportType(RunModeConstants.INDEPENDENCE.toString());
            runModeConfig.setEnvMap(new HashMap<>());
            runModeConfig.setOnSampleError(false);
            // 执行配置
            RunModeConfigDTO config = new RunModeConfigDTO();
            config.setMode(RunModeConstants.PARALLEL.toString());
            if (!StringUtils.equalsAnyIgnoreCase(reports.get(0).getActuator(), StorageEnums.LOCAL.name())) {
                config.setResourcePoolId(reports.get(0).getActuator());
            }

            Map<String, ApiScenarioReportResult> reportMap = new HashMap<>();
            reports.forEach(item -> {
                ApiScenarioReportResult reportResult = new ApiScenarioReportResult();
                BeanUtils.copyBean(reportResult, item);
                reportMap.put(item.getScenarioId(), reportResult);
            });
            RunScenarioRequest request = new RunScenarioRequest();
            request.setRerun(true);
            request.setReportMap(reportMap);

            request.setTriggerMode(testPlanReport.getTriggerMode());
            request.setExecuteType(ExecuteType.Completed.name());
            request.setRunMode(ApiRunMode.SCENARIO_PLAN.name());
            request.setIds(scenarioIds); // 场景IDS
            request.setPlanScenarioIds(planScenarioIds);

            // 一批执行的报告配置都是相同的
            ApiScenarioReportWithBLOBs scenario = apiScenarioReportMapper.selectByPrimaryKey(reportIds.get(0));
            if (scenario != null && StringUtils.isNotEmpty(scenario.getEnvConfig())) {
                request.setConfig(JSON.parseObject(scenario.getEnvConfig(), RunModeConfigDTO.class));
            } else {
                request.setConfig(runModeConfig);
            }
            request.setTestPlanReportId(parametersDTO.getReportId());
            request.setId(UUID.randomUUID().toString());
            request.setRequestOriginator(CommonConstants.TEST_PLAN);

            LoggerUtil.info("清理原始报告对应结果：【" + request.getTestPlanReportId() + "】," + reportIds.size());
            ApiScenarioReportResultExample reportResultExample = new ApiScenarioReportResultExample();
            reportResultExample.createCriteria().andReportIdIn(reportIds);
            apiScenarioReportResultMapper.deleteByExample(reportResultExample);

            ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
            structureExample.createCriteria().andReportIdIn(reportIds);
            apiScenarioReportStructureMapper.deleteByExample(structureExample);

            LoggerUtil.info("重跑测试计划报告：【" + request.getTestPlanReportId() + "】,开始执行：" + reportIds.size());
            isStart = true;
            apiScenarioExecuteService.run(request);
        }
        return isStart;
    }

}
