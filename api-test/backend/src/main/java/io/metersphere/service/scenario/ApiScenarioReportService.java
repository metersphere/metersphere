package io.metersphere.service.scenario;

import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.ApiScenarioReportInitDTO;
import io.metersphere.api.dto.automation.ApiScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.jmeter.utils.ReportStatusUtil;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportResultMapper;
import io.metersphere.base.mapper.plan.TestPlanApiScenarioMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiHomeFilterEnum;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.enums.ExecutionExecuteTypeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.commons.vo.ResultVO;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.BaseShareInfoService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.ServiceUtils;
import io.metersphere.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
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
    private NoticeSendService noticeSendService;
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
    private ExtApiScenarioReportResultMapper extApiScenarioReportResultMapper;
    @Resource
    private ApiScenarioExecutionInfoService scenarioExecutionInfoService;
    @Resource
    private BaseUserService userService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    BaseShareInfoService baseShareInfoService;

    public void saveResult(ResultDTO dto) {
        // 报告详情内容
        apiScenarioReportResultService.save(dto.getReportId(), dto.getRequestResults());
    }

    public void batchSaveResult(List<ResultDTO> dtos) {
        apiScenarioReportResultService.batchSave(dtos);
    }

    public ApiScenarioReportResult get(String reportId, boolean selectReportContent) {
        ApiScenarioReportResult reportResult = extApiScenarioReportMapper.get(reportId);
        if (reportResult != null) {
            if (reportResult.getReportVersion() != null && reportResult.getReportVersion() > 1) {
                reportResult.setContent(JSON.toJSONString(apiScenarioReportStructureService.assembleReport(reportId, selectReportContent)));
            } else {
                ApiScenarioReportDetail detail = apiScenarioReportDetailMapper.selectByPrimaryKey(reportId);
                if (detail != null && reportResult != null) {
                    reportResult.setContent(new String(detail.getContent(), StandardCharsets.UTF_8));
                }
            }
            return reportResult;
        }
        // case 集成报告
        ApiScenarioReportResult result = this.getApiIntegrated(reportId);
        return result;
    }

    /**
     * CASE集成报告
     *
     * @param reportId
     * @return
     */
    public ApiScenarioReportResult getApiIntegrated(String reportId) {
        ApiDefinitionExecResultWithBLOBs result = definitionExecResultMapper.selectByPrimaryKey(reportId);
        if (result != null) {
            ApiScenarioReportResult reportResult = new ApiScenarioReportResult();
            BeanUtils.copyBean(reportResult, result);
            reportResult.setReportVersion(2);
            reportResult.setTestId(reportId);
            ApiScenarioReportDTO dto = apiScenarioReportStructureService.apiIntegratedReport(reportId);
            apiScenarioReportStructureService.initProjectEnvironmentByEnvConfig(dto, result.getEnvConfig());
            apiScenarioReportStructureService.getEnvConfig(dto, result.getEnvConfig());
            reportResult.setContent(JSON.toJSONString(dto));
            return reportResult;
        }
        return null;
    }

    public List<ApiScenarioReportResult> list(QueryAPIReportRequest request) {
        request = this.initRequest(request);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiScenarioReportResult> list = extApiScenarioReportMapper.list(request);
        List<String> userIds = list.stream().map(ApiScenarioReportResult::getUserId).collect(Collectors.toList());
        Map<String, User> userMap = ServiceUtils.getUserMap(userIds);
        list.forEach(item -> {
            User user = userMap.get(item.getUserId());
            if (user != null) item.setUserName(user.getName());
        });
        return list;
    }

    public QueryAPIReportRequest initRequest(QueryAPIReportRequest request) {
        if (request != null) {
            //初始化triggerMode的查询条件： 如果查询API的话，增加 JENKINS_RUN_TEST_PLAN(jenkins调用测试计划时执行的场景) 查询条件
            if (MapUtils.isNotEmpty(request.getFilters()) && request.getFilters().containsKey(CommonConstants.TRIGGER_MODE)
                    && CollectionUtils.isNotEmpty(request.getFilters().get(CommonConstants.TRIGGER_MODE))
                    && request.getFilters().get(CommonConstants.TRIGGER_MODE).contains("API")
                    && !request.getFilters().get(CommonConstants.TRIGGER_MODE).contains(ReportTriggerMode.JENKINS_RUN_TEST_PLAN.name())) {
                request.getFilters().get(CommonConstants.TRIGGER_MODE).add(ReportTriggerMode.JENKINS_RUN_TEST_PLAN.name());
            }

            if (StringUtils.startsWith(request.getSelectDataRange(), "scheduleExecution")) {
                if (request.getFilters() == null) {
                    request.setFilters(new HashMap<>() {{
                        this.put(CommonConstants.TRIGGER_MODE, new ArrayList<>() {{
                            this.add(ReportTriggerMode.SCHEDULE.name());
                        }});
                    }});
                } else {
                    if (request.getFilters().containsKey(CommonConstants.TRIGGER_MODE)
                            && request.getFilters().get(CommonConstants.TRIGGER_MODE).contains(ReportTriggerMode.SCHEDULE.name())) {
                        request.getFilters().get(CommonConstants.TRIGGER_MODE).add(ReportTriggerMode.SCHEDULE.name());
                    } else {
                        request.getFilters().put(CommonConstants.TRIGGER_MODE, new ArrayList<>() {{
                            this.add(ReportTriggerMode.SCHEDULE.name());
                        }});
                    }
                }
                List<String> statusList = request.getFilters().get(CommonConstants.STATUS);
                if (statusList == null) {
                    statusList = new ArrayList<>();
                }
                switch (request.getSelectDataRange()) {
                    case ApiHomeFilterEnum.SCHEDULE_EXECUTION_PASS:
                        statusList.add(ApiReportStatus.SUCCESS.name());
                        break;
                    case ApiHomeFilterEnum.SCHEDULE_EXECUTION_FAKE_ERROR:
                        statusList.add(ApiReportStatus.FAKE_ERROR.name());
                        break;
                    case ApiHomeFilterEnum.SCHEDULE_EXECUTION_FAILED:
                        statusList.add(ApiReportStatus.ERROR.name());
                        break;
                }
                if (CollectionUtils.isNotEmpty(statusList)) {
                    request.getFilters().put(CommonConstants.STATUS, statusList);
                }
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

    private void checkNameExist(ApiScenarioReportResult request) {
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andNameEqualTo(request.getName()).
                andProjectIdEqualTo(request.getProjectId()).
                andExecuteTypeEqualTo(ExecuteType.Saved.name()).
                andIdNotEqualTo(request.getId());

        if (apiScenarioReportMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }
    }

    public ApiScenarioReportResult init(String scenarioIds, String reportName, String status, String scenarioNames, String triggerMode, String projectId, String userID) {
        ApiScenarioReportResult report = new ApiScenarioReportResult();
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
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals(CommonConstants.CASE)) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        apiScenarioReportMapper.insert(report);
        return report;
    }

    public ApiScenarioReportWithBLOBs editReport(String reportType, String reportId, String status, String runMode) {
        ApiScenarioReportWithBLOBs report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
        if (report == null) {
            report = new ApiScenarioReportWithBLOBs();
            report.setId(reportId);
        }
        if (StringUtils.equals(reportType, RunModeConstants.SET_REPORT.toString())
                || StringUtils.equals(report.getStatus(), ApiReportStatus.STOPPED.name())) {
            return report;
        }
        if (StringUtils.equals(runMode, CommonConstants.CASE)) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        report.setStatus(status);
        report.setName(report.getScenarioName() + "-" + DateUtils.getTimeStr(System.currentTimeMillis()));
        report.setEndTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals(CommonConstants.CASE)) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        return report;
    }

    public ApiScenarioReport updateReport(ApiScenarioReportResult test) {
        checkNameExist(test);
        ApiScenarioReportWithBLOBs report = new ApiScenarioReportWithBLOBs();
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
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals(CommonConstants.CASE)) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        return report;
    }

    public ApiScenarioReport updatePlanCase(ResultDTO dto) {
        ResultVO resultVO = ReportStatusUtil.computedProcess(dto);
        ApiScenarioReport report = editReport(dto.getReportType(), dto.getReportId(), resultVO.getStatus(), dto.getRunMode());
        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(dto.getTestId());
        if (testPlanApiScenario != null) {
            if (report != null) {
                testPlanApiScenario.setLastResult(report.getStatus());
                report.setScenarioId(testPlanApiScenario.getApiScenarioId());
            } else {
                testPlanApiScenario.setLastResult(resultVO.getStatus());
            }
            String passRate = resultVO.computerPassRate();
            testPlanApiScenario.setPassRate(passRate);
            testPlanApiScenario.setReportId(dto.getReportId());
            testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
            testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);

            // 更新场景状态
            ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
            if (scenario != null) {
                scenario.setLastResult(resultVO.getStatus());
                scenario.setPassRate(passRate);
                scenario.setReportId(dto.getReportId());
                int executeTimes = 0;
                if (scenario.getExecuteTimes() != null) {
                    executeTimes = scenario.getExecuteTimes().intValue();
                }
                scenario.setExecuteTimes(executeTimes + 1);
                apiScenarioMapper.updateByPrimaryKey(scenario);
            }
        }
        return report;
    }

    public ApiScenarioReport updateSchedulePlanCase(ResultDTO dto) {
        List<String> testPlanReportIdList = new ArrayList<>();

        ResultVO resultVO = ReportStatusUtil.computedProcess(dto);
        ApiScenarioReport report = editReport(dto.getReportType(), dto.getReportId(), resultVO.getStatus(), dto.getRunMode());
        if (report != null) {
            if (StringUtils.isNotEmpty(dto.getTestPlanReportId()) && !testPlanReportIdList.contains(dto.getTestPlanReportId())) {
                testPlanReportIdList.add(dto.getTestPlanReportId());
            }
            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(dto.getTestId());
            if (testPlanApiScenario != null) {
                testPlanApiScenario.setLastResult(report.getStatus());
                String passRate = resultVO.computerPassRate();
                testPlanApiScenario.setPassRate(resultVO.computerPassRate());

                testPlanApiScenario.setReportId(report.getId());
                testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
                testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);

                // 更新场景状态
                ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
                if (scenario != null) {
                    scenario.setLastResult(resultVO.getStatus());
                    scenario.setPassRate(passRate);
                    scenario.setReportId(report.getId());
                    int executeTimes = 0;
                    if (scenario.getExecuteTimes() != null) {
                        executeTimes = scenario.getExecuteTimes().intValue();
                    }
                    scenario.setExecuteTimes(executeTimes + 1);
                    apiScenarioMapper.updateByPrimaryKey(scenario);
                }
            }
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
                if (StringUtils.equalsIgnoreCase(status, ApiReportStatus.ERROR.name())) {
                    hasError = true;
                } else if (StringUtils.equalsIgnoreCase(status, ApiReportStatus.FAKE_ERROR.name())) {
                    hasErrorReport = true;
                } else if (StringUtils.equalsIgnoreCase(status, ApiReportStatus.STOPPED.name())) {
                    hasStop = true;
                } else if (StringUtils.equalsIgnoreCase(status, ApiReportStatus.PENDING.name())) {
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

        return hasError ? ApiReportStatus.ERROR.name() : hasErrorReport ? ApiReportStatus.FAKE_ERROR.name() : hasStop ? ApiReportStatus.STOPPED.name() : hasUnExecute ? ApiReportStatus.PENDING.name() : ApiReportStatus.SUCCESS.name();
    }

    public void margeReport(String reportId, String runMode, String console) {
        // 更新场景状态
        boolean isActuator = false;
        if (StringUtils.equalsIgnoreCase(runMode, ApiRunMode.DEFINITION.name())) {
            ApiDefinitionExecResultWithBLOBs result = definitionExecResultMapper.selectByPrimaryKey(reportId);
            if (!StringUtils.equalsAnyIgnoreCase(result.getStatus(), ApiReportStatus.RERUNNING.name())) {
                result.setEndTime(System.currentTimeMillis());
            }
            List<String> statusList = extApiDefinitionExecResultMapper.selectDistinctStatusByReportId(reportId);
            result.setStatus(this.getIntegrationReportStatus(statusList));
            definitionExecResultMapper.updateByPrimaryKeySelective(result);
            isActuator = !StringUtils.equals(result.getActuator(), StorageConstants.LOCAL.name());
        } else {
            ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
            if (report != null) {
                if (!StringUtils.equalsAnyIgnoreCase(report.getStatus(), ApiReportStatus.RERUNNING.name())) {
                    report.setEndTime(System.currentTimeMillis());
                }
                List<String> statusList = extApiScenarioReportResultMapper.selectDistinctStatusByReportId(reportId);
                report.setStatus(this.getIntegrationReportStatus(statusList));
                // 更新报告
                apiScenarioReportMapper.updateByPrimaryKey(report);
                //场景集合报告，按照集合报告的结果作为场景的最后执行结果
                scenarioExecutionInfoService.insertExecutionInfoByScenarioIds(report.getScenarioId(), report.getStatus(), report.getTriggerMode(), report.getProjectId(), ExecutionExecuteTypeEnum.BASIC.name());
                isActuator = !StringUtils.equals(report.getActuator(), StorageConstants.LOCAL.name());
            }
        }

        console = StringUtils.isNotEmpty(console) ? console : FixedCapacityUtil.getJmeterLogger(reportId, true);
        if (StringUtils.isNotEmpty(console) && !isActuator) {
            apiScenarioReportStructureService.update(reportId, console, false);
        }
        // 更新控制台信息
        FixedCapacityUtil.remove(reportId);
    }

    public String getEnvironment(ApiScenarioWithBLOBs apiScenario) {
        String environment = "未配置";
        String environmentType = apiScenario.getEnvironmentType();
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.name()) && StringUtils.isNotEmpty(apiScenario.getEnvironmentJson())) {
            String environmentJson = apiScenario.getEnvironmentJson();
            JSONObject jsonObject = JSONUtil.parseObject(environmentJson);
            ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
            List<String> collect = jsonObject.toMap().values().stream().map(Object::toString).collect(Collectors.toList());
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

    public void sendNotice(ApiScenarioWithBLOBs scenario, ApiScenarioReport result) {

        BeanMap beanMap = new BeanMap(scenario);

        String event;
        String status;
        if (StringUtils.endsWithIgnoreCase(scenario.getLastResult(), ApiReportStatus.SUCCESS.name())) {
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
        String shareUrl = getScenarioShareUrl(result.getId(), userId);
        paramMap.put("scenarioShareUrl", baseSystemConfigDTO.getUrl() + "/api/share-api-report" + shareUrl);
        String context = "${operator}执行接口自动化" + status + ": ${name}";
        NoticeModel noticeModel = NoticeModel.builder().operator(userId).context(context).subject("接口自动化通知").paramMap(paramMap).event(event).build();

        Project project = projectMapper.selectByPrimaryKey(scenario.getProjectId());
        noticeSendService.send(project, NoticeConstants.TaskType.API_AUTOMATION_TASK, noticeModel);
    }

    public String getScenarioShareUrl(String scenarioReportId, String userId) {
        ShareInfo shareRequest = new ShareInfo();
        shareRequest.setCustomData(scenarioReportId);
        shareRequest.setShareType(ShareType.API_REPORT.name());
        shareRequest.setCreateUserId(userId);
        ShareInfo shareInfo = baseShareInfoService.generateShareInfo(shareRequest);
        return baseShareInfoService.conversionShareInfoToDTO(shareInfo).getShareUrl();
    }

    public String update(ApiScenarioReportResult test) {
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
                ApiReportBatchRequest reportRequest = new ApiReportBatchRequest();
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

    public void deleteAPIReportBatch(ApiReportBatchRequest reportRequest) {
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

    public List<String> getIdsByDeleteBatchRequest(ApiReportBatchRequest reportRequest) {
        List<String> ids = reportRequest.getIds();
        if (reportRequest.isSelectAllDate()) {
            ids = this.idList(reportRequest);
            if (reportRequest.getUnSelectIds() != null) {
                ids.removeAll(reportRequest.getUnSelectIds());
            }
        }
        return ids;
    }

    public List<String> batchDeleteReportResource(ApiReportBatchRequest reportRequest, List<String> ids, boolean deleteApiResult) {
        List<String> myList = reportRequest.getIds().stream().distinct().collect(Collectors.toList());
        reportRequest.setIds(myList);
        //为预防数量太多，调用删除方法时引起SQL过长的Bug，此处采取分批执行的方式。
        //每次处理的数据数量
        int handleCount = 2000;
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

    public long countByProjectIdAndCreateAndByScheduleInThisWeek(String projectId, String executeType, String version) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioReportMapper.countByProjectIdAndCreateAndByScheduleInThisWeek(projectId, executeType, version, firstTime.getTime(), lastTime.getTime());
        }
    }

    public long countByProjectIdAndCreateInThisWeek(String projectId, String executeType, String version) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioReportMapper.countByProjectIdAndCreateInThisWeek(projectId, executeType, version, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countByProjectIdGroupByExecuteResult(String projectId, String executeType, String version) {
        return extApiScenarioReportMapper.countByProjectIdGroupByExecuteResult(projectId, executeType, version);
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

    public ApiScenarioReportResult init(ApiScenarioReportInitDTO initModel) {
        if (initModel == null) {
            initModel = new ApiScenarioReportInitDTO();
        }
        ApiScenarioReportResult report = new ApiScenarioReportResult();
        if (StringUtils.equalsAny(initModel.getTriggerMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.DEFINITION.name())) {
            initModel.setTriggerMode(ReportTriggerMode.MANUAL.name());
        }
        report.setId(initModel.getId());
        report.setTestId(initModel.getId());
        String scenarioName = initModel.getScenarioName();
        if (StringUtils.isNotEmpty(scenarioName)) {
            scenarioName = scenarioName.length() >= 3000 ? scenarioName.substring(0, 2000) : scenarioName;
            report.setName(scenarioName);
        } else {
            report.setName("场景调试");
        }
        report.setUpdateTime(System.currentTimeMillis());
        report.setCreateTime(System.currentTimeMillis());

        String status = initModel.getConfig() != null && StringUtils.equals(initModel.getConfig().getMode(),
                RunModeConstants.SERIAL.toString()) ? ApiReportStatus.PENDING.name() : ApiReportStatus.RUNNING.name();
        report.setStatus(status);
        if (StringUtils.isNotEmpty(initModel.getUserId())) {
            report.setUserId(initModel.getUserId());
            report.setCreateUser(initModel.getUserId());
        } else {
            report.setUserId(SessionUtils.getUserId());
            report.setCreateUser(SessionUtils.getUserId());
        }
        if (initModel.getConfig() != null && StringUtils.isNotBlank(initModel.getConfig().getResourcePoolId())) {
            report.setActuator(initModel.getConfig().getResourcePoolId());
        } else {
            report.setActuator(StorageEnums.LOCAL.name());
        }
        report.setTriggerMode(initModel.getTriggerMode());
        report.setReportVersion(2);
        report.setExecuteType(initModel.getExecType());
        report.setProjectId(initModel.getProjectId());
        report.setScenarioName(scenarioName);
        report.setScenarioId(initModel.getScenarioId());
        if (initModel.getConfig() != null) {
            report.setEnvConfig(JSON.toJSONString(initModel.getConfig()));
        }
        report.setRelevanceTestPlanReportId(initModel.getRelevanceTestPlanReportId());
        report.setReportType(ReportTypeConstants.SCENARIO_INDEPENDENT.name());
        return report;
    }

    public ApiScenarioReportResult getApiScenarioReportResult(RunScenarioRequest request, String serialReportId, String scenarioNames, String reportScenarioIds) {
        ApiScenarioReportResult report = this.init(new ApiScenarioReportInitDTO(request.getConfig().getReportId(), reportScenarioIds, scenarioNames, request.getTriggerMode(), ExecuteType.Saved.name(), request.getProjectId(), request.getReportUserID(), request.getConfig(), request.getTestPlanReportId()));
        report.setName(request.getConfig().getReportName());
        report.setId(serialReportId);
        report.setReportType(ReportTypeConstants.SCENARIO_INTEGRATED.name());
        request.getConfig().setAmassReport(serialReportId);
        if (request.getConfig() != null) {
            report.setEnvConfig(JSON.toJSONString(request.getConfig()));
        }
        report.setStatus(ApiReportStatus.RUNNING.name());
        return report;
    }

    public void cleanUpReport(long time, String projectId) {
        List<String> ids = extApiScenarioReportMapper.selectByProjectIdAndLessThanTime(projectId, time);
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiReportBatchRequest request = new ApiReportBatchRequest();
            request.setIds(ids);
            request.setSelectAllDate(false);
            request.setCaseType(ReportTypeConstants.SCENARIO.name());
            deleteAPIReportBatch(request);
        }
        List<String> definitionExecIds = extApiDefinitionExecResultMapper.selectByProjectIdAndLessThanTime(projectId, time);
        if (CollectionUtils.isNotEmpty(definitionExecIds)) {
            ApiReportBatchRequest request = new ApiReportBatchRequest();
            request.setIds(definitionExecIds);
            request.setSelectAllDate(false);
            request.setCaseType(ReportTypeConstants.API.name());
            deleteAPIReportBatch(request);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchSave(Map<String, RunModeDataDTO> executeQueue, String serialReportId, String runMode, List<MsExecResponseDTO> responseDTOS) {
        List<ApiScenarioReportResult> list = new LinkedList<>();
        if (StringUtils.isEmpty(serialReportId)) {
            for (String reportId : executeQueue.keySet()) {
                ApiScenarioReportResult report = executeQueue.get(reportId).getReport();
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
            ApiDefinitionExecResultWithBLOBs result = definitionExecResultMapper.selectByPrimaryKey(reportRequest.getId());
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

    public ApiScenarioReportResult initResult(String reportId, String testPlanScenarioId, String name, RunScenarioRequest request) {
        return this.init(new ApiScenarioReportInitDTO(reportId, testPlanScenarioId, name, request.getTriggerMode(), request.getExecuteType(), request.getProjectId(), request.getReportUserID(), request.getConfig(), request.getTestPlanReportId()));
    }

    public ApiScenarioReportResult initDebugResult(RunDefinitionRequest request) {
        return this.init(new ApiScenarioReportInitDTO(request.getId(), request.getScenarioId(), request.getScenarioName(), ReportTriggerMode.MANUAL.name(), request.getExecuteType(), request.getProjectId(), SessionUtils.getUserId(), request.getConfig(), null));
    }

    public List<PlanReportCaseDTO> selectForPlanReport(List<String> reportIds) {
        return extApiScenarioReportMapper.selectForPlanReport(reportIds);
    }

    public Map<String, String> selectResultByIdList(List<String> reportIdList) {
        Map<String, String> returnMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(reportIdList)) {
            List<ApiScenarioReport> apiDefinitionExecResultList = extApiScenarioReportMapper.selectStatusByIds(reportIdList);
            if (CollectionUtils.isNotEmpty(apiDefinitionExecResultList)) {
                returnMap = apiDefinitionExecResultList.stream().collect(Collectors.toMap(ApiScenarioReport::getId, ApiScenarioReport::getStatus, (k1, k2) -> k1));
            }
        }
        return returnMap;
    }
}
