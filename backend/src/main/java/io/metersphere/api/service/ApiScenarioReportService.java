package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.APIReportBatchRequest;
import io.metersphere.api.dto.ApiScenarioReportDTO;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.exec.scenario.TestPlanResourceService;
import io.metersphere.api.jmeter.FixedCapacityUtils;
import io.metersphere.api.service.utils.PassRateUtil;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExample;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.domain.ApiScenarioReportDetail;
import io.metersphere.base.domain.ApiScenarioReportDetailExample;
import io.metersphere.base.domain.ApiScenarioReportExample;
import io.metersphere.base.domain.ApiScenarioReportResultExample;
import io.metersphere.base.domain.ApiScenarioReportResultWithBLOBs;
import io.metersphere.base.domain.ApiScenarioReportStructureExample;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironment;
import io.metersphere.base.domain.ApiTestEnvironmentExample;
import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportDetailMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.ApiScenarioReportResultMapper;
import io.metersphere.base.mapper.ApiScenarioReportStructureMapper;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.EnvironmentGroupMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportResultMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.ApiReportCountDTO;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.PlanReportCaseDTO;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.xmind.utils.FileUtil;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportService {
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioReportDetailMapper apiScenarioReportDetailMapper;
    @Resource
    private ApiScenarioReportResultMapper apiScenarioReportResultMapper;
    @Resource
    private ApiScenarioReportResultService apiScenarioReportResultService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private UiScenarioMapper uiScenarioMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private UserService userService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private ApiScenarioReportStructureService apiScenarioReportStructureService;
    @Resource
    private ApiScenarioReportStructureMapper apiScenarioReportStructureMapper;
    @Resource
    private ApiDefinitionExecResultMapper definitionExecResultMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private UiReportServiceProxy uiReportServiceProxy;
    @Resource
    private ExtApiScenarioReportResultMapper extApiScenarioReportResultMapper;
    @Resource
    private RedisTemplateService redisTemplateService;
    @Resource
    private TestPlanResourceService testPlanResourceService;

    public void saveResult(ResultDTO dto) {
        // 报告详情内容
        apiScenarioReportResultService.save(dto.getReportId(), dto.getRequestResults());
    }

    public void batchSaveResult(List<ResultDTO> dtos) {
        apiScenarioReportResultService.batchSave(dtos);
    }

    public void saveUiResult(List<RequestResult> requestResults, ResultDTO dto) {
        // 报告详情内容
        uiReportServiceProxy.saveUiResult(dto.getReportId(), requestResults);
    }

    public ApiScenarioReport testEnded(ResultDTO dto) {
        if (!StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            // 更新控制台信息
            apiScenarioReportStructureService.update(dto.getReportId(), dto.getConsole());
        }
        // 优化当前执行携带结果作为状态判断依据
        ApiScenarioReport scenarioReport;
        if (StringUtils.equals(dto.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
            scenarioReport = updatePlanCase(dto);
        } else if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            scenarioReport = updateSchedulePlanCase(dto);
        } else if (dto.getRunMode().startsWith("UI")) {
            ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
            example.createCriteria().andReportIdEqualTo(dto.getReportId());
            scenarioReport = updateUiScenario(apiScenarioReportResultMapper.selectByExampleWithBLOBs(example), dto);
        } else {
            scenarioReport = updateScenario(dto);
        }
        // 串行队列
        return scenarioReport;
    }

    public APIScenarioReportResult get(String reportId, boolean selectReportContent) {
        ApiDefinitionExecResult result = definitionExecResultMapper.selectByPrimaryKey(reportId);
        if (result != null) {
            APIScenarioReportResult reportResult = new APIScenarioReportResult();
            BeanUtils.copyBean(reportResult, result);
            reportResult.setReportVersion(2);
            reportResult.setTestId(reportId);
            ApiScenarioReportDTO dto = apiScenarioReportStructureService.apiIntegratedReport(reportId);
            reportResult.setContent(JSON.toJSONString(dto));
            return reportResult;
        }
        APIScenarioReportResult reportResult = extApiScenarioReportMapper.get(reportId);
        if (reportResult != null) {
            if (reportResult.getReportVersion() != null && reportResult.getReportVersion() > 1) {
                reportResult.setContent(JSON.toJSONString(apiScenarioReportStructureService.assembleReport(reportId, selectReportContent)));
            } else {
                ApiScenarioReportDetail detail = apiScenarioReportDetailMapper.selectByPrimaryKey(reportId);
                if (detail != null && reportResult != null) {
                    reportResult.setContent(new String(detail.getContent(), StandardCharsets.UTF_8));
                }
            }
        }
        return reportResult;
    }

    public List<APIScenarioReportResult> list(QueryAPIReportRequest request) {
        request = this.initRequest(request);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<APIScenarioReportResult> list = extApiScenarioReportMapper.list(request);
        List<String> userIds = list.stream().map(APIScenarioReportResult::getUserId)
                .collect(Collectors.toList());
        Map<String, User> userMap = ServiceUtils.getUserMap(userIds);
        list.forEach(item -> {
            User user = userMap.get(item.getUserId());
            if (user != null)
                item.setUserName(user.getName());
        });
        return list;
    }

    public QueryAPIReportRequest initRequest(QueryAPIReportRequest request) {
        if (request != null) {
            //初始化triggerMode的查询条件： 如果查询API的话，增加 JENKINS_RUN_TEST_PLAN(jenkins调用测试计划时执行的场景) 查询条件
            if (MapUtils.isNotEmpty(request.getFilters()) && request.getFilters().containsKey("trigger_mode")
                    && CollectionUtils.isNotEmpty(request.getFilters().get("trigger_mode"))
                    && request.getFilters().get("trigger_mode").contains("API") && !request.getFilters().get("trigger_mode").contains(ReportTriggerMode.JENKINS_RUN_TEST_PLAN.name())) {
                request.getFilters().get("trigger_mode").add(ReportTriggerMode.JENKINS_RUN_TEST_PLAN.name());
            }
        }
        return request;
    }

    public List<String> idList(QueryAPIReportRequest request) {
        request = this.initRequest(request);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        //检查必填参数caseType
        if (request.getIsUi()) {
            //ui报告对应的用例类型也是scenario
            request.setCaseType(ReportTypeConstants.SCENARIO.name());
        }
        if (StringUtils.equalsAny(request.getCaseType(), ReportTypeConstants.API.name(), ReportTypeConstants.SCENARIO.name())) {
            return extApiScenarioReportMapper.idList(request);
        } else {
            return new ArrayList<>(0);
        }
    }

    private void checkNameExist(APIScenarioReportResult request) {
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andExecuteTypeEqualTo(ExecuteType.Saved.name()).andIdNotEqualTo(request.getId());
        if (apiScenarioReportMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }
    }

    public APIScenarioReportResult init(String scenarioIds, String reportName, String status, String scenarioNames, String triggerMode, String projectId, String userID) {
        APIScenarioReportResult report = new APIScenarioReportResult();
        if (triggerMode.equals(ApiRunMode.SCENARIO.name()) || triggerMode.equals(ApiRunMode.DEFINITION.name())) {
            triggerMode = ReportTriggerMode.MANUAL.name();
        }
        report.setId(UUID.randomUUID().toString());
        report.setName(reportName);
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(status);
        if (StringUtils.isNotEmpty(userID)) {
            report.setUserId(userID);
        } else {
            report.setUserId(SessionUtils.getUserId());
        }
        report.setTriggerMode(triggerMode);
        report.setExecuteType(ExecuteType.Saved.name());
        report.setProjectId(projectId);
        report.setScenarioName(scenarioNames);
        report.setScenarioId(scenarioIds);
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        apiScenarioReportMapper.insert(report);
        return report;
    }

    public ApiScenarioReport editReport(String reportType, String reportId, String status, String runMode, TestPlanApiScenario planScenario) {
        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
        if (report == null) {
            report = new ApiScenarioReport();
            report.setId(reportId);
        }
        if (StringUtils.equals(reportType, RunModeConstants.SET_REPORT.toString())) {
            return report;
        }
        if (StringUtils.equals(runMode, "CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        report.setStatus(status);
        report.setName(report.getScenarioName() + "-" + DateUtils.getTimeStr(System.currentTimeMillis()));
        report.setEndTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        // UI 调试类型报告不记录更新状态
        if (report.getExecuteType().equals(ExecuteType.Debug.name()) &&
                report.getReportType().equals(ReportTypeConstants.UI_INDEPENDENT.name())) {
            return report;
        }
        if (planScenario != null) {
            report.setScenarioId(planScenario.getApiScenarioId());
            report.setEndTime(System.currentTimeMillis());
        }
        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        return report;
    }

    public ApiScenarioReport updateReport(APIScenarioReportResult test) {
        checkNameExist(test);
        ApiScenarioReport report = new ApiScenarioReport();
        report.setId(test.getId());
        report.setProjectId(test.getProjectId());
        report.setName(test.getName());
        report.setScenarioName(test.getScenarioName());
        report.setScenarioId(test.getScenarioId());
        report.setTriggerMode(test.getTriggerMode());
        report.setDescription(test.getDescription());
        report.setEndTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(test.getStatus());
        report.setUserId(test.getUserId());
        report.setExecuteType(test.getExecuteType());
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        return report;
    }

    public ApiScenarioReport updatePlanCase(ResultDTO dto) {
        String status = getStatus(dto);
        ApiScenarioReport report = editReport(dto.getReportType(), dto.getReportId(), status, dto.getRunMode(), null);
        if (!redisTemplateService.hasReport(dto.getTestId(), report.getId())) {
            return report;
        }
        try {
            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(dto.getTestId());
            if (testPlanApiScenario != null) {
                if (report != null) {
                    testPlanApiScenario.setLastResult(report.getStatus());
                } else {
                    testPlanApiScenario.setLastResult(StringUtils.endsWithIgnoreCase(status, ScenarioStatus.Error.name())
                            ? ScenarioStatus.Fail.name() : status);

                }
                long successSize = dto.getRequestResults().stream().filter(requestResult ->
                        StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Success.name())).count();

                String passRate = new DecimalFormat("0%").format((float) successSize / dto.getRequestResults().size());
                passRate = StringUtils.equals(passRate, "NaN") ? "0%" : passRate;
                testPlanApiScenario.setPassRate(passRate);
                testPlanApiScenario.setReportId(dto.getReportId());
                testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
                testPlanResourceService.updatePlanScenario(testPlanApiScenario);

                // 更新场景状态
                ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
                if (scenario != null) {
                    scenario.setLastResult(StringUtils.endsWithIgnoreCase(status, ScenarioStatus.Error.name())
                            ? ScenarioStatus.Fail.name() : status);
                    scenario.setPassRate(passRate);
                    scenario.setReportId(dto.getReportId());
                    int executeTimes = 0;
                    if (scenario.getExecuteTimes() != null) {
                        executeTimes = scenario.getExecuteTimes().intValue();
                    }
                    scenario.setExecuteTimes(executeTimes + 1);
                    testPlanResourceService.updateScenario(scenario);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error(e);
        } finally {
            redisTemplateService.unlock(dto.getTestId(), dto.getReportId());
        }
        return report;
    }

    public ApiScenarioReport updateSchedulePlanCase(ResultDTO dto) {
        String status = getStatus(dto);
        TestPlanApiScenario planScenario = testPlanApiScenarioMapper.selectByPrimaryKey(dto.getTestId());
        ApiScenarioReport report = editReport(dto.getReportType(), dto.getReportId(), status, dto.getRunMode(), planScenario);
        if (!redisTemplateService.hasReport(dto.getTestId(), report.getId())) {
            return report;
        }
        try {
            if (report != null && planScenario != null) {
                planScenario.setLastResult(report.getStatus());
                long successSize = dto.getRequestResults().stream().filter(requestResult ->
                        StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Success.name())).count();
                String passRate = new DecimalFormat("0%").format((float) successSize / dto.getRequestResults().size());
                passRate = StringUtils.equals(passRate, "NaN") ? "0%" : passRate;
                planScenario.setPassRate(passRate);
                planScenario.setReportId(report.getId());
                planScenario.setUpdateTime(System.currentTimeMillis());
                testPlanResourceService.updatePlanScenario(planScenario);

                // 更新场景状态
                ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(planScenario.getApiScenarioId());
                if (scenario != null) {
                    scenario.setLastResult(StringUtils.endsWithIgnoreCase(status, ScenarioStatus.Error.name())
                            ? ScenarioStatus.Fail.name() : status);
                    scenario.setPassRate(passRate);
                    scenario.setReportId(report.getId());
                    int executeTimes = 0;
                    if (scenario.getExecuteTimes() != null) {
                        executeTimes = scenario.getExecuteTimes().intValue();
                    }
                    scenario.setExecuteTimes(executeTimes + 1);
                    testPlanResourceService.updateScenario(scenario);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error(e);
        } finally {
            redisTemplateService.unlock(dto.getTestId(), dto.getReportId());
        }
        return report;
    }

    private String getIntegrationReportStatus(List<String> reportStatus) {
        boolean hasError = false, hasErrorReport = false, hasUnExecute = false, hasOtherStatus = false, hasStop = false;

        if (CollectionUtils.isEmpty(reportStatus)) {
            //查不到任何结果，按照未执行来处理
            hasUnExecute = true;
        } else {
            for (String status : reportStatus) {
                if (StringUtils.equalsIgnoreCase(status, ExecuteResult.SCENARIO_ERROR.toString())) {
                    hasError = true;
                } else if (StringUtils.equalsIgnoreCase(status, ExecuteResult.ERROR_REPORT_RESULT.toString())) {
                    hasErrorReport = true;
                } else if (StringUtils.equalsIgnoreCase(status, ExecuteResult.STOP.toString())) {
                    hasStop = true;
                } else if (StringUtils.equalsIgnoreCase(status, ExecuteResult.UN_EXECUTE.toString())) {
                    hasUnExecute = true;
                } else {
                    hasOtherStatus = true;
                }
            }
            if (hasError || hasErrorReport || hasOtherStatus) {
                //根据状态优先级判定，只要存在失败/误报/其他待定状态 的数据， 则未执行和停止都为false （优先级最低）
                hasUnExecute = false;
                hasStop = false;
            }
        }

        return hasError ? ScenarioStatus.Error.name() :
                hasErrorReport ? ExecuteResult.ERROR_REPORT_RESULT.toString() :
                        hasStop ? ExecuteResult.STOP.toString() :
                                hasUnExecute ? ExecuteResult.UN_EXECUTE.toString() : ScenarioStatus.Success.name();
    }

    public void margeReport(String reportId, String runMode, String console) {
        // 更新场景状态
        List<String> statusList = null;
        if (StringUtils.equalsIgnoreCase(runMode, ApiRunMode.DEFINITION.name())) {
            ApiDefinitionExecResult result = definitionExecResultMapper.selectByPrimaryKey(reportId);
            statusList = extApiDefinitionExecResultMapper.selectDistinctStatusByReportId(reportId);
            result.setStatus(this.getIntegrationReportStatus(statusList));
            result.setEndTime(System.currentTimeMillis());
            definitionExecResultMapper.updateByPrimaryKeySelective(result);
        } else {
            ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
            if (report != null) {
                statusList = extApiScenarioReportResultMapper.selectDistinctStatusByReportId(reportId);
                report.setStatus(this.getIntegrationReportStatus(statusList));
                report.setEndTime(System.currentTimeMillis());
                // 更新报告
                apiScenarioReportMapper.updateByPrimaryKey(report);
            }
        }

        console = StringUtils.isNotEmpty(console) ? console : FixedCapacityUtils.getJmeterLogger(reportId, true);
        if (StringUtils.isNotEmpty(console)) {
            apiScenarioReportStructureService.update(reportId, console);
        }
        // 更新控制台信息
        if (FixedCapacityUtils.jmeterLogTask.containsKey(reportId)) {
            FixedCapacityUtils.jmeterLogTask.remove(reportId);
        }
    }

    public ApiScenarioReport updateScenario(ResultDTO dto) {
        // 更新报告状态
        String status = getStatus(dto);
        ApiScenarioReport report = editReport(dto.getReportType(), dto.getReportId(), status, dto.getRunMode(), null);
        // 更新场景状态
        ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(dto.getTestId());
        if (scenario == null) {
            scenario = apiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
        }
        if (scenario != null) {
            scenario.setLastResult(StringUtils.endsWithIgnoreCase(status, ScenarioStatus.Error.name())
                    ? ScenarioStatus.Fail.name() : status);
            long successSize = dto.getRequestResults().stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Success.name())).count();
            if (dto.getRequestResults().size() == 0) {
                scenario.setPassRate("0%");
            } else {
                scenario.setPassRate(new DecimalFormat("0%").format((float) successSize / dto.getRequestResults().size()));
            }

            scenario.setReportId(dto.getReportId());
            int executeTimes = 0;
            if (scenario.getExecuteTimes() != null) {
                executeTimes = scenario.getExecuteTimes().intValue();
            }
            scenario.setExecuteTimes(executeTimes + 1);
            apiScenarioMapper.updateByPrimaryKey(scenario);
        }

        // 发送通知
        if (scenario != null && report != null) {
            sendNotice(scenario, report);
        }
        return report;
    }

    public ApiScenarioReport updateUiScenario(List<ApiScenarioReportResultWithBLOBs> requestResults, ResultDTO dto) {
        long errorSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Error.name())).count();
        // 更新报告状态
        String status = getStatus(dto);

        ApiScenarioReport report = editReport(dto.getReportType(), dto.getReportId(), status, dto.getRunMode(), null);
        // 更新场景状态
        UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(dto.getTestId());
        if (scenario == null) {
            scenario = uiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
        }
        if (scenario != null) {
            if (StringUtils.equalsAnyIgnoreCase(status, ExecuteResult.ERROR_REPORT_RESULT.toString())) {
                scenario.setLastResult(status);
            } else {
                scenario.setLastResult(errorSize > 0 ? ScenarioStatus.Fail.name() : ScenarioStatus.Success.name());
            }

            scenario.setPassRate(PassRateUtil.calculatePassRate(requestResults, report));
            scenario.setReportId(dto.getReportId());
            int executeTimes = 0;
            if (scenario.getExecuteTimes() != null) {
                executeTimes = scenario.getExecuteTimes().intValue();
            }
            scenario.setExecuteTimes(executeTimes + 1);
            // 针对 UI 调试类型的不需要更新
            if (report.getExecuteType().equals(ExecuteType.Debug.name()) &&
                    report.getReportType().equals(ReportTypeConstants.UI_INDEPENDENT.name())) {
                return report;
            }
            uiScenarioMapper.updateByPrimaryKey(scenario);
        }

//        // 发送通知
//        if (scenario != null && report != null) {
//            sendNotice(scenario, report);
//        }
        return report;
    }

    public String getEnvironment(ApiScenarioWithBLOBs apiScenario) {
        String environment = "未配置";
        String environmentType = apiScenario.getEnvironmentType();
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.name()) && StringUtils.isNotEmpty(apiScenario.getEnvironmentJson())) {
            String environmentJson = apiScenario.getEnvironmentJson();
            JSONObject jsonObject = JSON.parseObject(environmentJson, Feature.DisableSpecialKeyDetect);
            ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
            List<String> collect = jsonObject.values().stream().map(Object::toString).collect(Collectors.toList());
            collect.add("-1");// 防止没有配置环境导致不能发送的问题
            example.createCriteria().andIdIn(collect);
            List<ApiTestEnvironment> envs = apiTestEnvironmentMapper.selectByExample(example);
            String env = envs.stream().map(ApiTestEnvironment::getName).collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(env)) {
                environment = env;
            }
        }

        if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())) {
            String environmentGroupId = apiScenario.getEnvironmentGroupId();
            EnvironmentGroup environmentGroup = environmentGroupMapper.selectByPrimaryKey(environmentGroupId);
            if (environmentGroup != null) {
                environment = environmentGroup.getName();
            }
        }
        return environment;
    }

    private void sendNotice(ApiScenarioWithBLOBs scenario, ApiScenarioReport result) {

        BeanMap beanMap = new BeanMap(scenario);

        String event;
        String status;
        if (StringUtils.equals(scenario.getLastResult(), ScenarioStatus.Success.name())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
            status = "成功";
        } else {
            event = NoticeConstants.Event.EXECUTE_FAILED;
            status = "失败";
        }
        String userId = result.getCreateUser();
        UserDTO userDTO = userService.getUserDTO(userId);
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        assert systemParameterService != null;
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put("operator", userDTO.getName());
        paramMap.put("status", scenario.getLastResult());
        paramMap.put("environment", getEnvironment(scenario));
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + result.getId();
        paramMap.put("reportUrl", reportUrl);
        String context = "${operator}执行接口自动化" + status + ": ${name}";
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(userId)
                .context(context)
                .subject("接口自动化通知")
                .paramMap(paramMap)
                .event(event)
                .build();

        Project project = projectMapper.selectByPrimaryKey(scenario.getProjectId());
        noticeSendService.send(project, NoticeConstants.TaskType.API_AUTOMATION_TASK, noticeModel);
    }

    public String update(APIScenarioReportResult test) {
        ApiScenarioReport report = updateReport(test);
        ApiScenarioReportDetail detail = apiScenarioReportDetailMapper.selectByPrimaryKey(test.getId());
        if (detail == null) {
            detail = new ApiScenarioReportDetail();
            detail.setContent(test.getContent().getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(test.getProjectId());
            apiScenarioReportDetailMapper.insert(detail);
        } else {
            detail.setContent(test.getContent().getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(test.getProjectId());
            apiScenarioReportDetailMapper.updateByPrimaryKey(detail);
        }
        return report.getId();
    }

    public static List<String> getReportIds(String content) {
        try {
            return JSON.parseObject(content, List.class);
        } catch (Exception e) {
            return null;
        }
    }


    public void delete(DeleteAPIReportRequest request) {

        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(request.getId());

        deleteScenarioReportResource(request.getId());

        deleteApiDefinitionResult(request.getId());

        // 补充逻辑，如果是集成报告则把零时报告全部删除
        if (report != null && StringUtils.isNotEmpty(report.getScenarioId())) {
            List<String> list = getReportIds(report.getScenarioId());
            if (CollectionUtils.isNotEmpty(list)) {
                APIReportBatchRequest reportRequest = new APIReportBatchRequest();
                reportRequest.setIsUi(request.getIsUi());
                reportRequest.setIds(list);
                reportRequest.setCaseType(ReportTypeConstants.SCENARIO.name());
                this.deleteAPIReportBatch(reportRequest);
            }
        }
    }

    public void deleteScenarioReportResource(String id) {
        apiScenarioReportMapper.deleteByPrimaryKey(id);

        apiScenarioReportDetailMapper.deleteByPrimaryKey(id);

        ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
        example.createCriteria().andReportIdEqualTo(id);
        apiScenarioReportResultMapper.deleteByExample(example);

        ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
        structureExample.createCriteria().andReportIdEqualTo(id);
        apiScenarioReportStructureMapper.deleteByExample(structureExample);
    }

    public void delete(String id) {
        apiScenarioReportDetailMapper.deleteByPrimaryKey(id);
        apiScenarioReportMapper.deleteByPrimaryKey(id);
        ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
        example.createCriteria().andReportIdEqualTo(id);
        apiScenarioReportResultMapper.deleteByExample(example);

        ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
        structureExample.createCriteria().andReportIdEqualTo(id);
        apiScenarioReportStructureMapper.deleteByExample(structureExample);

        ApiDefinitionExecResultExample definitionExecResultExample = new ApiDefinitionExecResultExample();
        definitionExecResultExample.createCriteria().andIdEqualTo(id);
        definitionExecResultMapper.deleteByExample(definitionExecResultExample);

        ApiDefinitionExecResultExample execResultExample = new ApiDefinitionExecResultExample();
        execResultExample.createCriteria().andIntegratedReportIdEqualTo(id);
        definitionExecResultMapper.deleteByExample(execResultExample);

    }

    public void deleteByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            deleteScenarioReportByIds(ids);
            deleteApiDefinitionResultByIds(ids);
        }
    }

    public void deleteAPIReportBatch(APIReportBatchRequest reportRequest) {
        List<String> ids = getIdsByDeleteBatchRequest(reportRequest);
        ids = batchDeleteReportResource(reportRequest, ids, true);
        //处理报告关联数据
        if (!ids.isEmpty()) {
            deleteScenarioReportByIds(ids);
            deleteApiDefinitionResultByIds(ids);
        }
    }

    public void deleteScenarioReportByIds(List<String> ids) {
        ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
        detailExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportDetailMapper.deleteByExample(detailExample);

        ApiScenarioReportExample apiTestReportExample = new ApiScenarioReportExample();
        apiTestReportExample.createCriteria().andIdIn(ids);
        apiScenarioReportMapper.deleteByExample(apiTestReportExample);

        ApiScenarioReportResultExample reportResultExample = new ApiScenarioReportResultExample();
        reportResultExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportResultMapper.deleteByExample(reportResultExample);

        ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
        structureExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportStructureMapper.deleteByExample(structureExample);
    }

    private void deleteApiDefinitionResultByIds(List<String> ids) {
        ApiDefinitionExecResultExample definitionExecResultExample = new ApiDefinitionExecResultExample();
        definitionExecResultExample.createCriteria().andIdIn(ids);
        definitionExecResultMapper.deleteByExample(definitionExecResultExample);

        ApiDefinitionExecResultExample execResultExample = new ApiDefinitionExecResultExample();
        execResultExample.createCriteria().andIntegratedReportIdIn(ids);
        definitionExecResultMapper.deleteByExample(execResultExample);
    }

    private void deleteApiDefinitionResult(String id) {
        ApiDefinitionExecResultExample definitionExecResultExample = new ApiDefinitionExecResultExample();
        definitionExecResultExample.createCriteria().andIdEqualTo(id);
        definitionExecResultMapper.deleteByExample(definitionExecResultExample);

        ApiDefinitionExecResultExample execResultExample = new ApiDefinitionExecResultExample();
        execResultExample.createCriteria().andIntegratedReportIdEqualTo(id);
        definitionExecResultMapper.deleteByExample(execResultExample);
    }

    public List<String> getIdsByDeleteBatchRequest(APIReportBatchRequest reportRequest) {
        List<String> ids = reportRequest.getIds();
        if (reportRequest.isSelectAllDate()) {
            ids = this.idList(reportRequest);
            if (reportRequest.getUnSelectIds() != null) {
                ids.removeAll(reportRequest.getUnSelectIds());
            }
        }
        return ids;
    }

    public List<String> batchDeleteReportResource(APIReportBatchRequest reportRequest, List<String> ids, boolean deleteApiResult) {
        List<String> myList = reportRequest.getIds().stream().distinct().collect(Collectors.toList());
        reportRequest.setIds(myList);
        //为预防数量太多，调用删除方法时引起SQL过长的Bug，此处采取分批执行的方式。
        //每次处理的数据数量
        int handleCount = 5000;
        //每次处理的集合
        while (ids.size() > handleCount) {
            List<String> handleIdList = new ArrayList<>(handleCount);
            List<String> otherIdList = new ArrayList<>();
            for (int index = 0; index < ids.size(); index++) {
                if (index < handleCount) {
                    handleIdList.add(ids.get(index));
                } else {
                    otherIdList.add(ids.get(index));
                }
            }
            //处理本次的数据
            deleteScenarioReportByIds(handleIdList);

            if (deleteApiResult) {
                deleteApiDefinitionResultByIds(handleIdList);
            }

            //转存剩余的数据
            ids = otherIdList;
        }
        return ids;
    }

    public long countByProjectIdAndCreateAndByScheduleInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioReportMapper.countByProjectIdAndCreateAndByScheduleInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public long countByProjectIdAndCreateInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioReportMapper.countByProjectIdAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countByProjectIdGroupByExecuteResult(String projectId) {
        return extApiScenarioReportMapper.countByProjectIdGroupByExecuteResult(projectId);
    }

    public List<ApiScenarioReport> selectLastReportByIds(List<String> ids) {
        if (!ids.isEmpty()) {
            return extApiScenarioReportMapper.selectLastReportByIds(ids);
        } else {
            return new ArrayList<>(0);
        }
    }

    public String getLogDetails(String id) {
        ApiScenarioReport bloBs = apiScenarioReportMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, ModuleReference.moduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(ids)) {
            ApiScenarioReportExample example = new ApiScenarioReportExample();
            example.createCriteria().andIdIn(ids);
            List<ApiScenarioReport> reportList = apiScenarioReportMapper.selectByExample(example);
            List<String> names = reportList.stream().map(ApiScenarioReport::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), reportList.get(0).getProjectId(), String.join(",", names), reportList.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public List<ApiScenarioReport> getByIds(List<String> ids) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(ids)) {
            ApiScenarioReportExample example = new ApiScenarioReportExample();
            example.createCriteria().andIdIn(ids);
            return apiScenarioReportMapper.selectByExample(example);
        }
        return null;
    }

    public List<ApiReportCountDTO> countByApiScenarioId() {
        return extApiScenarioReportMapper.countByApiScenarioId();
    }

    public Map<String, String> getReportStatusByReportIds(Collection<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>();
        List<ApiScenarioReport> reportList = extApiScenarioReportMapper.selectStatusByIds(values);
        for (ApiScenarioReport report : reportList) {
            map.put(report.getId(), report.getStatus());
        }
        return map;
    }

    public APIScenarioReportResult init(String id, String scenarioId, String scenarioName, String triggerMode, String execType, String projectId, String userID, RunModeConfigDTO config) {
        APIScenarioReportResult report = new APIScenarioReportResult();
        if (triggerMode.equals(ApiRunMode.SCENARIO.name()) || triggerMode.equals(ApiRunMode.DEFINITION.name())) {
            triggerMode = ReportTriggerMode.MANUAL.name();
        }
        report.setId(id);
        report.setTestId(id);
        if (StringUtils.isNotEmpty(scenarioName)) {
            scenarioName = scenarioName.length() >= 3000 ? scenarioName.substring(0, 2000) : scenarioName;
            report.setName(scenarioName);
        } else {
            report.setName("场景调试");
        }
        report.setUpdateTime(System.currentTimeMillis());
        report.setCreateTime(System.currentTimeMillis());

        String status = config != null && StringUtils.equals(config.getMode(), RunModeConstants.SERIAL.toString())
                ? APITestStatus.Waiting.name() : APITestStatus.Running.name();
        report.setStatus(status);
        if (StringUtils.isNotEmpty(userID)) {
            report.setUserId(userID);
            report.setCreateUser(userID);
        } else {
            report.setUserId(SessionUtils.getUserId());
            report.setCreateUser(SessionUtils.getUserId());
        }
        if (config != null && StringUtils.isNotBlank(config.getResourcePoolId())) {
            report.setActuator(config.getResourcePoolId());
        } else {
            report.setActuator("LOCAL");
        }
        report.setTriggerMode(triggerMode);
        report.setReportVersion(2);
        report.setExecuteType(execType);
        report.setProjectId(projectId);
        report.setScenarioName(scenarioName);
        report.setScenarioId(scenarioId);
        report.setReportType(ReportTypeConstants.SCENARIO_INDEPENDENT.name());
        return report;
    }

    private String getStatus(ResultDTO dto) {
        if (MapUtils.isNotEmpty(dto.getArbitraryData()) && dto.getArbitraryData().containsKey("REPORT_STATUS")) {
            // 资源池执行整体传输失败，单条传输内容，获取资源池执行统计的状态
            return String.valueOf(dto.getArbitraryData().get("REPORT_STATUS"));
        }
        long errorSize = dto.getRequestResults().stream().filter(requestResult ->
                StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Error.name())).count();

        long errorReportResultSize = dto.getRequestResults().stream().filter(requestResult ->
                StringUtils.equalsIgnoreCase(requestResult.getStatus(), ExecuteResult.ERROR_REPORT_RESULT.toString())).count();
        //类型为ui时的统计
        if (StringUtils.isNotEmpty(dto.getRunMode()) && dto.getRunMode().startsWith(ReportTypeConstants.UI.name())) {
            try {
                errorSize = getUiErrorSize(dto);
            } catch (Exception e) {
                // UI 返回的结果在 headers 里面，格式不符合规范的直接认定结果为失败
                errorSize = 1;
            }
        }
        String status = dto.getRequestResults().isEmpty() ? ExecuteResult.UN_EXECUTE.toString() : ScenarioStatus.Success.name();
        if (errorSize > 0) {
            status = ScenarioStatus.Error.name();
        } else if (errorReportResultSize > 0) {
            status = ExecuteResult.ERROR_REPORT_RESULT.toString();
        }
        // 超时状态
        if (dto != null && dto.getArbitraryData() != null && dto.getArbitraryData().containsKey("TIMEOUT") && (Boolean) dto.getArbitraryData().get("TIMEOUT")) {
            LoggerUtil.info("资源 " + dto.getTestId() + " 执行超时", dto.getReportId());
            status = ScenarioStatus.Timeout.name();
        }
        return status;
    }

    /**
     * 主流程或者有断言失败的就算失败
     *
     * @param dto
     * @return
     */
    private long getUiErrorSize(ResultDTO dto) {
        int errorSize = 0;
        boolean success = false;
        String processType = null;
        String cmdName = null;
        for (RequestResult r : dto.getRequestResults()) {
            if (StringUtils.isNotEmpty(r.getResponseResult().getHeaders())) {
                JSONArray responseArr = JSONArray.parseArray(r.getResponseResult().getHeaders());
                for (int i = 0; i < responseArr.size(); i++) {
                    JSONObject stepResult = responseArr.getJSONObject(i);
                    success = Optional.ofNullable(stepResult.getBoolean("success")).orElse(Boolean.FALSE);
                    processType = Optional.ofNullable(stepResult.getString("processType")).orElse("");
                    cmdName = Optional.ofNullable(stepResult.getString("cmdName")).orElse("");
                    if (!success && (StringUtils.equalsIgnoreCase("MAIN", processType) || cmdName.startsWith("verify") || cmdName.startsWith("assert"))) {
                        errorSize++;
                    }
                }
            }
        }
        return errorSize;
    }

    public List<PlanReportCaseDTO> selectForPlanReport(List<String> reportIds) {
        return extApiScenarioReportMapper.selectForPlanReport(reportIds);
    }

    public void cleanUpReport(long time, String projectId) {
        List<String> ids = extApiScenarioReportMapper.selectByProjectIdAndLessThanTime(projectId, time);
        int pageSize = 100;
        int total = ids.size();
        long pages = Double.valueOf(Math.ceil(total / (double) pageSize)).longValue();
        for (int i = 0; i < pages; i++) {
            List<String> subIds;
            if (i == pages - 1) {
                // 最后一页
                subIds = ids.subList(i * pageSize, (i * pageSize) + (total % pageSize));
            } else {
                subIds = ids.subList(i * pageSize, i * pageSize + pageSize);
            }

            if (CollectionUtils.isNotEmpty(subIds)) {
                APIReportBatchRequest request = new APIReportBatchRequest();
                request.setIds(subIds);
                request.setSelectAllDate(false);
                request.setCaseType(ReportTypeConstants.SCENARIO.name());
                deleteAPIReportBatch(request);
            }
        }

        List<String> definitionExecIds = extApiDefinitionExecResultMapper.selectByProjectIdAndLessThanTime(projectId, time);
        total = definitionExecIds.size();
        pages = Double.valueOf(Math.ceil(total / (double) pageSize)).longValue();
        for (int i = 0; i < pages; i++) {
            List<String> subIds;
            if (i == pages - 1) {
                // 最后一页
                subIds = definitionExecIds.subList(i * pageSize, (i * pageSize) + (total % pageSize));
            } else {
                subIds = definitionExecIds.subList(i * pageSize, i * pageSize + pageSize);
            }
            if (CollectionUtils.isNotEmpty(subIds)) {
                APIReportBatchRequest request = new APIReportBatchRequest();
                request.setIds(subIds);
                request.setSelectAllDate(false);
                request.setCaseType(ReportTypeConstants.API.name());
                deleteAPIReportBatch(request);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchSave(Map<String, RunModeDataDTO> executeQueue, String serialReportId, String runMode, List<MsExecResponseDTO> responseDTOS) {
        List<APIScenarioReportResult> list = new LinkedList<>();
        if (StringUtils.isEmpty(serialReportId)) {
            for (String reportId : executeQueue.keySet()) {
                APIScenarioReportResult report = executeQueue.get(reportId).getReport();
                list.add(report);
                responseDTOS.add(new MsExecResponseDTO(executeQueue.get(reportId).getTestId(), reportId, runMode));
            }
            if (CollectionUtils.isNotEmpty(list)) {
                extApiScenarioReportMapper.sqlInsert(list);
            }
        }
    }

    public void reName(ApiScenarioReport reportRequest) {
        if (StringUtils.equalsAnyIgnoreCase(reportRequest.getReportType(), ReportTypeConstants.API_INDEPENDENT.name(), ReportTypeConstants.API_INTEGRATED.name())) {
            ApiDefinitionExecResult result = definitionExecResultMapper.selectByPrimaryKey(reportRequest.getId());
            if (result != null) {
                result.setName(reportRequest.getName());
                definitionExecResultMapper.updateByPrimaryKeySelective(result);
            }
        } else {
            ApiScenarioReport apiTestReport = apiScenarioReportMapper.selectByPrimaryKey(reportRequest.getId());
            if (apiTestReport != null) {
                apiTestReport.setName(reportRequest.getName());
                apiScenarioReportMapper.updateByPrimaryKey(apiTestReport);
            }
        }
    }

    public RequestResult selectReportContent(String stepId) {
        return apiScenarioReportStructureService.selectReportContent(stepId);
    }

    public void cleanUpUiReportImg(List<String> ids) {
        try {
            if (ids != null && CollectionUtils.isNotEmpty(ids)) {
                for (String id : ids) {
                    if (FileUtil.deleteDir(new File(FileUtils.UI_IMAGE_DIR + "/" + id))) {
                        LogUtil.info("删除 UI 报告截图成功，报告 ID 为 ：" + id);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }
}
