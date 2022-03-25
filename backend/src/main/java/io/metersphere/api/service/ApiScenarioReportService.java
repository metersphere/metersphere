package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.jmeter.FixedCapacityUtils;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
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
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportService {
    Logger testPlanLog = LoggerFactory.getLogger("testPlanExecuteLog");
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

    public void saveResult(List<RequestResult> requestResults, ResultDTO dto) {
        // 报告详情内容
        apiScenarioReportResultService.save(dto.getReportId(), requestResults);
    }

    public void batchSaveResult(List<ResultDTO> dtos) {
        apiScenarioReportResultService.batchSave(dtos);
    }

    public ApiScenarioReport testEnded(ResultDTO dto) {
        if (!StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            // 更新控制台信息
            apiScenarioReportStructureService.update(dto.getReportId(), dto.getConsole());
        }
        ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
        example.createCriteria().andReportIdEqualTo(dto.getReportId());
        List<ApiScenarioReportResult> requestResults = apiScenarioReportResultMapper.selectByExample(example);

        if (StringUtils.isNotEmpty(dto.getTestPlanReportId())) {
            String status = getStatus(requestResults, dto);
            Map<String, String> reportMap = new HashMap<String, String>() {{
                this.put(dto.getReportId(), status);
            }};
            testPlanLog.info("TestPlanReportId" + JSONArray.toJSONString(dto.getReportId()) + " EXECUTE OVER. SCENARIO STATUS : " + JSONObject.toJSONString(reportMap));
        }

        ApiScenarioReport scenarioReport;
        if (StringUtils.equals(dto.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
            scenarioReport = updatePlanCase(requestResults, dto);
        } else if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            scenarioReport = updateSchedulePlanCase(requestResults, dto);
        } else {
            scenarioReport = updateScenario(requestResults, dto);
        }
        // 串行队列
        return scenarioReport;
    }

    public APIScenarioReportResult get(String reportId) {
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
                reportResult.setContent(JSON.toJSONString(apiScenarioReportStructureService.assembleReport(reportId)));
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

    public List<String> idList(QueryAPIReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extApiScenarioReportMapper.idList(request);
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

    public ApiScenarioReport editReport(String reportType, String reportId, String status, String runMode) {
        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
        if (report == null) {
            report = new ApiScenarioReport();
            report.setId(reportId);
        }
        if (StringUtils.equals(reportType, RunModeConstants.SET_REPORT.toString())) {
            return report;
        }
        if (runMode.equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        report.setStatus(status);
        report.setName(report.getScenarioName() + "-" + DateUtils.getTimeStr(System.currentTimeMillis()));
        report.setEndTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
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

    public ApiScenarioReport updatePlanCase(List<ApiScenarioReportResult> requestResults, ResultDTO dto) {
        long errorSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Error.name())).count();
        String status = getStatus(requestResults, dto);
        ApiScenarioReport report = editReport(dto.getReportType(), dto.getReportId(), status, dto.getRunMode());
        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(dto.getTestId());
        if (testPlanApiScenario != null) {
            if (report != null) {
                testPlanApiScenario.setLastResult(report.getStatus());
            } else {
                if (errorSize > 0) {
                    testPlanApiScenario.setLastResult(ScenarioStatus.Fail.name());
                } else {
                    testPlanApiScenario.setLastResult(ScenarioStatus.Success.name());
                }
            }
            long successSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Success.name())).count();

            String passRate = new DecimalFormat("0%").format((float) successSize / requestResults.size());
            testPlanApiScenario.setPassRate(passRate);
            testPlanApiScenario.setReportId(dto.getReportId());
            testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
            testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);

            // 更新场景状态
            ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
            if (scenario != null) {
                scenario.setLastResult(errorSize > 0 ? "Fail" : ScenarioStatus.Success.name());
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

    public ApiScenarioReport updateSchedulePlanCase(List<ApiScenarioReportResult> requestResults, ResultDTO dto) {
        List<String> testPlanReportIdList = new ArrayList<>();
        StringBuilder scenarioNames = new StringBuilder();

        long errorSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Error.name())).count();
        String status = getStatus(requestResults, dto);
        ApiScenarioReport report = editReport(dto.getReportType(), dto.getReportId(), status, dto.getRunMode());
        if (report != null) {
            if (StringUtils.isNotEmpty(dto.getTestPlanReportId()) && !testPlanReportIdList.contains(dto.getTestPlanReportId())) {
                testPlanReportIdList.add(dto.getTestPlanReportId());
            }
            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(dto.getTestId());
            if (testPlanApiScenario != null) {
                report.setScenarioId(testPlanApiScenario.getApiScenarioId());
                report.setEndTime(System.currentTimeMillis());
                apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                testPlanApiScenario.setLastResult(report.getStatus());
                long successSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Success.name())).count();
                String passRate = new DecimalFormat("0%").format((float) successSize / requestResults.size());
                testPlanApiScenario.setPassRate(passRate);

                testPlanApiScenario.setReportId(report.getId());
                report.setEndTime(System.currentTimeMillis());
                testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
                testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
                scenarioNames.append(report.getName()).append(",");

                // 更新场景状态
                ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
                if (scenario != null) {
                    if (errorSize > 0) {
                        scenario.setLastResult("Fail");
                    } else {
                        scenario.setLastResult(ScenarioStatus.Success.name());
                    }
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

    public void margeReport(String reportId, String runMode) {
        // 更新场景状态
        if (StringUtils.equalsIgnoreCase(runMode, ApiRunMode.DEFINITION.name())) {
            ApiDefinitionExecResult result = definitionExecResultMapper.selectByPrimaryKey(reportId);
            ApiDefinitionExecResultExample execResultExample = new ApiDefinitionExecResultExample();
            execResultExample.createCriteria().andIntegratedReportIdEqualTo(reportId).andStatusEqualTo("Error");
            long size = definitionExecResultMapper.countByExample(execResultExample);
            result.setStatus(size > 0 ? ScenarioStatus.Error.name() : ScenarioStatus.Success.name());
            definitionExecResultMapper.updateByPrimaryKeySelective(result);
        } else {
            ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
            if (report != null) {
                ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
                example.createCriteria().andReportIdEqualTo(reportId).andStatusEqualTo(ScenarioStatus.Error.name());
                long size = apiScenarioReportResultMapper.countByExample(example);
                report.setStatus(size > 0 ? ScenarioStatus.Error.name() : ScenarioStatus.Success.name());
                report.setEndTime(System.currentTimeMillis());
                // 更新报告
                apiScenarioReportMapper.updateByPrimaryKey(report);
            }
        }
        // 更新控制台信息
        apiScenarioReportStructureService.update(reportId, FixedCapacityUtils.getJmeterLogger(reportId));
    }

    public ApiScenarioReport updateScenario(List<ApiScenarioReportResult> requestResults, ResultDTO dto) {
        long errorSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Error.name())).count();
        // 更新报告状态
        String status = getStatus(requestResults, dto);

        ApiScenarioReport report = editReport(dto.getReportType(), dto.getReportId(), status, dto.getRunMode());
        // 更新场景状态
        ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(dto.getTestId());
        if (scenario == null) {
            scenario = apiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
        }
        if (scenario != null) {
            if (StringUtils.equalsAnyIgnoreCase(status, ExecuteResult.errorReportResult.name())) {
                scenario.setLastResult(status);
            } else {
                scenario.setLastResult(errorSize > 0 ? "Fail" : ScenarioStatus.Success.name());
            }

            long successSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Success.name())).count();
            if(requestResults.size() == 0){
                scenario.setPassRate("0%");
            }else {
                scenario.setPassRate(new DecimalFormat("0%").format((float) successSize / requestResults.size()));
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

    public String getEnvironment(ApiScenarioWithBLOBs apiScenario) {
        String environment = "未配置";
        String environmentType = apiScenario.getEnvironmentType();
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.name()) && StringUtils.isNotEmpty(apiScenario.getEnvironmentJson())) {
            String environmentJson = apiScenario.getEnvironmentJson();
            JSONObject jsonObject = JSON.parseObject(environmentJson);
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
                .successMailTemplate("api/ScenarioResultSuccess")
                .failedMailTemplate("api/ScenarioResultFailed")
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
        apiScenarioReportDetailMapper.deleteByPrimaryKey(request.getId());
        ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
        example.createCriteria().andReportIdEqualTo(request.getId());
        apiScenarioReportResultMapper.deleteByExample(example);

        ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
        structureExample.createCriteria().andReportIdEqualTo(request.getId());
        apiScenarioReportStructureMapper.deleteByExample(structureExample);

        ApiDefinitionExecResultExample definitionExecResultExample = new ApiDefinitionExecResultExample();
        definitionExecResultExample.createCriteria().andIdEqualTo(request.getId());
        definitionExecResultMapper.deleteByExample(definitionExecResultExample);

        ApiDefinitionExecResultExample execResultExample = new ApiDefinitionExecResultExample();
        execResultExample.createCriteria().andIntegratedReportIdEqualTo(request.getId());
        definitionExecResultMapper.deleteByExample(execResultExample);

        // 补充逻辑，如果是集成报告则把零时报告全部删除
        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(request.getId());
        if (report != null && StringUtils.isNotEmpty(report.getScenarioId())) {
            List<String> list = getReportIds(report.getScenarioId());
            if (CollectionUtils.isNotEmpty(list)) {
                APIReportBatchRequest reportRequest = new APIReportBatchRequest();
                reportRequest.setIds(list);
                this.deleteAPIReportBatch(reportRequest);
            }
        }
        apiScenarioReportMapper.deleteByPrimaryKey(request.getId());
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
            ApiScenarioReportExample example = new ApiScenarioReportExample();
            example.createCriteria().andIdIn(ids);
            ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
            detailExample.createCriteria().andReportIdIn(ids);
            apiScenarioReportDetailMapper.deleteByExample(detailExample);
            apiScenarioReportMapper.deleteByExample(example);

            ApiScenarioReportResultExample reportResultExample = new ApiScenarioReportResultExample();
            reportResultExample.createCriteria().andReportIdIn(ids);
            apiScenarioReportResultMapper.deleteByExample(reportResultExample);

            ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
            structureExample.createCriteria().andReportIdIn(ids);
            apiScenarioReportStructureMapper.deleteByExample(structureExample);

            ApiDefinitionExecResultExample definitionExecResultExample = new ApiDefinitionExecResultExample();
            definitionExecResultExample.createCriteria().andIdIn(ids);
            definitionExecResultMapper.deleteByExample(definitionExecResultExample);

            ApiDefinitionExecResultExample execResultExample = new ApiDefinitionExecResultExample();
            execResultExample.createCriteria().andIntegratedReportIdIn(ids);
            definitionExecResultMapper.deleteByExample(execResultExample);
        }
    }

    public void deleteAPIReportBatch(APIReportBatchRequest reportRequest) {
        List<String> ids = reportRequest.getIds();
        if (reportRequest.isSelectAllDate()) {
            ids = this.idList(reportRequest);
            if (reportRequest.getUnSelectIds() != null) {
                ids.removeAll(reportRequest.getUnSelectIds());
            }
        }
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andIdIn(reportRequest.getIds());
        List<ApiScenarioReport> reportList = apiScenarioReportMapper.selectByExample(example);
        // 取出可能是集成报告的ID 放入删除
        reportList.forEach(item -> {
            List<String> reportIds = getReportIds(item.getScenarioId());
            if (CollectionUtils.isNotEmpty(reportIds)) {
                reportRequest.getIds().addAll(reportIds);
            }
        });
        List<String> myList = reportRequest.getIds().stream().distinct().collect(Collectors.toList());
        reportRequest.setIds(myList);
        //为预防数量太多，调用删除方法时引起SQL过长的Bug，此处采取分批执行的方式。
        //每次处理的数据数量
        int handleCount = 7000;
        //每次处理的集合
        List<String> handleIdList = new ArrayList<>(handleCount);
        while (ids.size() > handleCount) {
            handleIdList = new ArrayList<>(handleCount);
            List<String> otherIdList = new ArrayList<>();
            for (int index = 0; index < ids.size(); index++) {
                if (index < handleCount) {
                    handleIdList.add(ids.get(index));
                } else {
                    otherIdList.add(ids.get(index));
                }
            }
            //处理本次的数据
            ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
            detailExample.createCriteria().andReportIdIn(handleIdList);
            apiScenarioReportDetailMapper.deleteByExample(detailExample);
            ApiScenarioReportExample apiTestReportExample = new ApiScenarioReportExample();
            apiTestReportExample.createCriteria().andIdIn(handleIdList);
            apiScenarioReportMapper.deleteByExample(apiTestReportExample);

            ApiScenarioReportResultExample reportResultExample = new ApiScenarioReportResultExample();
            reportResultExample.createCriteria().andReportIdIn(handleIdList);
            apiScenarioReportResultMapper.deleteByExample(reportResultExample);

            ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
            structureExample.createCriteria().andReportIdIn(handleIdList);
            apiScenarioReportStructureMapper.deleteByExample(structureExample);

            ApiDefinitionExecResultExample definitionExecResultExample = new ApiDefinitionExecResultExample();
            definitionExecResultExample.createCriteria().andIdIn(handleIdList);
            definitionExecResultMapper.deleteByExample(definitionExecResultExample);

            ApiDefinitionExecResultExample execResultExample = new ApiDefinitionExecResultExample();
            execResultExample.createCriteria().andIntegratedReportIdIn(handleIdList);
            definitionExecResultMapper.deleteByExample(execResultExample);

            //转存剩余的数据
            ids = otherIdList;
        }

        //处理最后剩余的数据
        if (!ids.isEmpty()) {
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

            ApiDefinitionExecResultExample definitionExecResultExample = new ApiDefinitionExecResultExample();
            definitionExecResultExample.createCriteria().andIdIn(ids);
            definitionExecResultMapper.deleteByExample(definitionExecResultExample);

            ApiDefinitionExecResultExample execResultExample = new ApiDefinitionExecResultExample();
            execResultExample.createCriteria().andIntegratedReportIdIn(ids);
            definitionExecResultMapper.deleteByExample(execResultExample);
        }
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

    private String getStatus(List<ApiScenarioReportResult> requestResults, ResultDTO dto) {
        long errorSize = 0;
        long errorReportResultSize = 0;
        for (ApiScenarioReportResult result : requestResults) {
            if (StringUtils.equalsIgnoreCase(result.getStatus(), ScenarioStatus.Error.name())) {
                errorSize++;
            } else if (StringUtils.equalsIgnoreCase(result.getStatus(), ExecuteResult.errorReportResult.name())) {
                errorReportResultSize++;
            }
        }
        String status;//增加误报状态判断
        if (errorSize > 0) {
            status = ScenarioStatus.Error.name();
        } else if (errorReportResultSize > 0) {
            status = ExecuteResult.errorReportResult.name();
        } else {
            status = ScenarioStatus.Success.name();
        }

        if (dto != null && dto.getArbitraryData() != null && dto.getArbitraryData().containsKey("TIMEOUT") && (Boolean) dto.getArbitraryData().get("TIMEOUT")) {
            LoggerUtil.info("报告 【 " + dto.getReportId() + " 】资源 " + dto.getTestId() + " 执行超时");
            status = ScenarioStatus.Timeout.name();
        }
        return status;
    }

    public List<PlanReportCaseDTO> selectForPlanReport(List<String> reportIds) {
        return extApiScenarioReportMapper.selectForPlanReport(reportIds);
    }

    public void cleanUpReport(long time, String projectId) {
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andCreateTimeLessThan(time).andProjectIdEqualTo(projectId);
        List<ApiScenarioReport> apiScenarioReports = apiScenarioReportMapper.selectByExample(example);
        List<String> ids = apiScenarioReports.stream().map(ApiScenarioReport::getId).collect(Collectors.toList());

        ApiDefinitionExecResultExample definitionExecResultExample = new ApiDefinitionExecResultExample();
        definitionExecResultExample.createCriteria().andCreateTimeLessThan(time).andProjectIdEqualTo(projectId);
        List<ApiDefinitionExecResult> apiDefinitionExecResults = definitionExecResultMapper.selectByExample(definitionExecResultExample);
        List<String> definitionExecIds = apiDefinitionExecResults.stream().map(ApiDefinitionExecResult::getId).collect(Collectors.toList());

        ids.addAll(definitionExecIds);
        if (CollectionUtils.isNotEmpty(ids)) {
            APIReportBatchRequest request = new APIReportBatchRequest();
            request.setIds(ids);
            request.setSelectAllDate(false);
            deleteAPIReportBatch(request);
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
        if (StringUtils.equalsIgnoreCase(reportRequest.getReportType(), ReportTypeConstants.API_INDEPENDENT.name())) {
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
}
