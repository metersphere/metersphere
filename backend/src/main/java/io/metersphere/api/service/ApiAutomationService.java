package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.ApiScenarioTestJob;
import io.metersphere.service.ScheduleService;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiAutomationService {
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiTestEnvironmentService environmentService;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ApiScenarioReportService apiReportService;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;

    public List<ApiScenarioDTO> list(ApiScenarioRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        if(request.isSelectThisWeedData()){
            Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
            Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
            if(weekFirstTime!=null){
                request.setCreateTime(weekFirstTime.getTime());
            }
        }
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(request);
        return list;
    }


    public List<String> selectIdsNotExistsInPlan(String projectId, String planId) {
        return extApiScenarioMapper.selectIdsNotExistsInPlan(projectId, planId);
    }

    public void deleteByIds(List<String> nodeIds) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andApiScenarioModuleIdIn(nodeIds);
        apiScenarioMapper.deleteByExample(example);
    }

    public ApiScenario create(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles) {
        request.setId(UUID.randomUUID().toString());
        checkNameExist(request);

        final ApiScenarioWithBLOBs scenario = new ApiScenarioWithBLOBs();
        scenario.setId(request.getId());
        scenario.setName(request.getName());
        scenario.setProjectId(request.getProjectId());
        scenario.setTags(request.getTags());
        scenario.setApiScenarioModuleId(request.getApiScenarioModuleId());
        scenario.setModulePath(request.getModulePath());
        scenario.setLevel(request.getLevel());
        scenario.setFollowPeople(request.getFollowPeople());
        scenario.setPrincipal(request.getPrincipal());
        scenario.setStepTotal(request.getStepTotal());
        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));
        scenario.setCreateTime(System.currentTimeMillis());
        scenario.setUpdateTime(System.currentTimeMillis());
        scenario.setNum(getNextNum(request.getProjectId()));
        if (StringUtils.isNotEmpty(request.getStatus())) {
            scenario.setStatus(request.getStatus());
        } else {
            scenario.setStatus(ScenarioStatus.Underway.name());
        }
        if (request.getUserId() == null) {
            scenario.setUserId(SessionUtils.getUserId());
        } else {
            scenario.setUserId(request.getUserId());
        }
        scenario.setDescription(request.getDescription());
        apiScenarioMapper.insert(scenario);

        List<String> bodyUploadIds = request.getBodyUploadIds();
        apiDefinitionService.createBodyFiles(bodyUploadIds, bodyFiles);
        return scenario;
    }

    private int getNextNum(String projectId) {
        ApiScenario apiScenario = extApiScenarioMapper.getNextNum(projectId);
        if (apiScenario == null) {
            return 100001;
        } else {
            return Optional.of(apiScenario.getNum() + 1).orElse(100001);
        }
    }

    public void update(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles) {
        checkNameExist(request);
        List<String> bodyUploadIds = request.getBodyUploadIds();
        apiDefinitionService.createBodyFiles(bodyUploadIds, bodyFiles);

        final ApiScenarioWithBLOBs scenario = new ApiScenarioWithBLOBs();
        scenario.setId(request.getId());
        scenario.setName(request.getName());
        scenario.setProjectId(request.getProjectId());
        scenario.setTags(request.getTags());
        scenario.setApiScenarioModuleId(request.getApiScenarioModuleId());
        scenario.setModulePath(request.getModulePath());
        scenario.setLevel(request.getLevel());
        scenario.setFollowPeople(request.getFollowPeople());
        scenario.setPrincipal(request.getPrincipal());
        scenario.setStepTotal(request.getStepTotal());
        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));
        scenario.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(request.getStatus())) {
            scenario.setStatus(request.getStatus());
        } else {
            scenario.setStatus(ScenarioStatus.Underway.name());
        }
        scenario.setUserId(request.getUserId());
        scenario.setDescription(request.getDescription());
        apiScenarioMapper.updateByPrimaryKeySelective(scenario);
    }

    public void delete(String id) {
        //及连删除外键表
        this.preDelete(id);
        apiScenarioMapper.deleteByPrimaryKey(id);
    }

    public void preDelete(String scenarioId) {
        List<String> ids = new ArrayList<>();
        ids.add(scenarioId);
        deleteApiScenarioReport(ids);

        scheduleService.deleteByResourceId(scenarioId);
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andApiScenarioIdEqualTo(scenarioId);
        List<TestPlanApiScenario> testPlanApiScenarioList = testPlanApiScenarioMapper.selectByExample(example);

        List<String> idList = new ArrayList<>(testPlanApiScenarioList.size());
        for (TestPlanApiScenario api :
                testPlanApiScenarioList) {
            idList.add(api.getId());
        }
        example = new TestPlanApiScenarioExample();

        if (!idList.isEmpty()) {
            example.createCriteria().andIdIn(idList);
            testPlanApiScenarioMapper.deleteByExample(example);
        }

    }

    private void deleteApiScenarioReport(List<String> scenarioIds) {
        ApiScenarioReportExample scenarioReportExample = new ApiScenarioReportExample();
        scenarioReportExample.createCriteria().andScenarioIdIn(scenarioIds);
        List<ApiScenarioReport> list = apiScenarioReportMapper.selectByExample(scenarioReportExample);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> ids = list.stream().map(ApiScenarioReport::getId).collect(Collectors.toList());
            DeleteAPIReportRequest reportRequest = new DeleteAPIReportRequest();
            reportRequest.setIds(ids);
            apiReportService.deleteAPIReportBatch(reportRequest);
        }
    }

    public void preDeleteBatch(List<String> scenarioIds) {
        deleteApiScenarioReport(scenarioIds);

        List<String> testPlanApiScenarioIdList = new ArrayList<>();
        for (String id : scenarioIds) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andApiScenarioIdEqualTo(id);
            List<TestPlanApiScenario> testPlanApiScenarioList = testPlanApiScenarioMapper.selectByExample(example);
            for (TestPlanApiScenario api : testPlanApiScenarioList) {
                if (!testPlanApiScenarioIdList.contains(api.getId())) {
                    testPlanApiScenarioIdList.add(api.getId());
                }
            }

            scheduleService.deleteByResourceId(id);
        }
        if (!testPlanApiScenarioIdList.isEmpty()) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andIdIn(testPlanApiScenarioIdList);
            testPlanApiScenarioMapper.deleteByExample(example);
        }

    }

    public void deleteBatch(List<String> ids) {
        // 删除外键表
        preDeleteBatch(ids);
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        apiScenarioMapper.deleteByExample(example);
    }

    public void removeToGc(List<String> apiIds) {
        extApiScenarioMapper.removeToGc(apiIds);
    }

    public void reduction(List<SaveApiScenarioRequest> requests) {
        List<String> apiIds = new ArrayList<>();
        requests.forEach(item -> {
            checkNameExist(item);
            apiIds.add(item.getId());
        });
        extApiScenarioMapper.reduction(apiIds);
    }

    private void checkNameExist(SaveApiScenarioRequest request) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andStatusNotEqualTo("Trash").andIdNotEqualTo(request.getId());
        if (apiScenarioMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("automation_name_already_exists"));
        }
    }

    public ApiScenarioWithBLOBs getApiScenario(String id) {
        return apiScenarioMapper.selectByPrimaryKey(id);
    }

    public List<ApiScenarioWithBLOBs> getApiScenarios(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return extApiScenarioMapper.selectIds(ids);
        }
        return new ArrayList<>();
    }

    private void createScenarioReport(String id, String scenarioId, String scenarioName, String triggerMode, String execType, String projectId, String userID) {
        APIScenarioReportResult report = new APIScenarioReportResult();
        report.setId(id);
        report.setTestId(id);
        if (StringUtils.isNotEmpty(scenarioName)) {
            report.setName(scenarioName);
        } else {
            report.setName("场景调试");
        }
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(APITestStatus.Running.name());
        if (StringUtils.isNotEmpty(userID)) {
            report.setUserId(userID);
        } else {
            report.setUserId(SessionUtils.getUserId());
        }
        report.setTriggerMode(triggerMode);
        report.setExecuteType(execType);
        report.setProjectId(projectId);
        report.setScenarioName(scenarioName);
        report.setScenarioId(scenarioId);
        apiScenarioReportMapper.insert(report);
    }

    /**
     * 场景测试执行
     *
     * @param request
     * @return
     */
    public String run(RunScenarioRequest request) {
        List<ApiScenarioWithBLOBs> apiScenarios = null;
        List<String> ids = request.getScenarioIds();
        if (request.isSelectAllDate()) {
            ids = this.getAllScenarioIdsByFontedSelect(
                    request.getModuleIds(), request.getName(), request.getProjectId(), request.getFilters(), request.getUnSelectIds());
        }
        apiScenarios = extApiScenarioMapper.selectIds(ids);
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        HashTree jmeterHashTree = new ListedHashTree();
        try {
            boolean isFirst = true;
            for (ApiScenarioWithBLOBs item : apiScenarios) {
                if (item.getStepTotal() == 0) {
                    // 只有一个场景且没有测试步骤，则提示
                    if (apiScenarios.size() == 1) {
                        MSException.throwException((item.getName() + "，" + Translator.get("automation_exec_info")));
                    }
                    LogUtil.warn(item.getName() + "，" + Translator.get("automation_exec_info"));
                    continue;
                }
                MsThreadGroup group = new MsThreadGroup();
                group.setLabel(item.getName());
                group.setName(UUID.randomUUID().toString());
                // 批量执行的结果直接存储为报告
                if (isFirst) {
                    group.setName(request.getId());
                    isFirst = false;
                }
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JSONObject element = JSON.parseObject(item.getScenarioDefinition());
                MsScenario scenario = JSONObject.parseObject(item.getScenarioDefinition(), MsScenario.class);

                // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
                if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                    LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                            new TypeReference<LinkedList<MsTestElement>>() {});
                    scenario.setHashTree(elements);
                }
                if (StringUtils.isNotEmpty(element.getString("variables"))) {
                    LinkedList<KeyValue> variables = mapper.readValue(element.getString("variables"),
                            new TypeReference<LinkedList<KeyValue>>() {});
                    scenario.setVariables(variables);
                }
                group.setEnableCookieShare(scenario.isEnableCookieShare());
                LinkedList<MsTestElement> scenarios = new LinkedList<>();
                scenarios.add(scenario);
                // 创建场景报告
                createScenarioReport(group.getName(), item.getId(), item.getName(), request.getTriggerMode() == null ? ReportTriggerMode.MANUAL.name() : request.getTriggerMode(),
                        request.getExecuteType(), item.getProjectId(), request.getReportUserID());
                group.setHashTree(scenarios);
                testPlan.getHashTree().add(group);

            }
        } catch (Exception ex) {
            MSException.throwException(ex.getMessage());
        }

        testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());
        String runMode = ApiRunMode.SCENARIO.name();
        if (StringUtils.isNotBlank(request.getRunMode()) && StringUtils.equals(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
            runMode = ApiRunMode.SCENARIO_PLAN.name();
        }
        // 调用执行方法
        jMeterService.runDefinition(request.getId(), jmeterHashTree, request.getReportId(), runMode);
        return request.getId();
    }

    /**
     * 获取前台查询条件查询的所有(未经分页筛选)数据ID
     *
     * @param moduleIds   模块ID_前台查询时所选择的
     * @param name        搜索条件_名称_前台查询时所输入的
     * @param projectId   所属项目_前台查询时所在项目
     * @param filters     过滤集合__前台查询时的过滤条件
     * @param unSelectIds 未勾选ID_前台没有勾选的ID
     * @return
     */
    private List<String> getAllScenarioIdsByFontedSelect(List<String> moduleIds, String name, String projectId, List<String> filters, List<String> unSelectIds) {
        ApiScenarioRequest selectRequest = new ApiScenarioRequest();
        selectRequest.setModuleIds(moduleIds);
        selectRequest.setName(name);
        selectRequest.setProjectId(projectId);
        selectRequest.setFilters(filters);
        selectRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(selectRequest);
        List<String> allIds = list.stream().map(ApiScenarioDTO::getId).collect(Collectors.toList());
        List<String> ids = allIds.stream().filter(id -> !unSelectIds.contains(id)).collect(Collectors.toList());
        return ids;
    }

    /**
     * 场景测试执行
     *
     * @param request
     * @param bodyFiles
     * @return
     */
    public String debugRun(RunDefinitionRequest request, List<MultipartFile> bodyFiles) {
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        apiDefinitionService.createBodyFiles(bodyUploadIds, bodyFiles);
        EnvironmentConfig envConfig = null;
        if (request.getEnvironmentId() != null) {
            ApiTestEnvironmentWithBLOBs environment = environmentService.get(request.getEnvironmentId());
            envConfig = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
        }
        ParameterConfig config = new ParameterConfig();
        config.setConfig(envConfig);
        HashTree hashTree = request.getTestElement().generateHashTree(config);
        // 调用执行方法
        createScenarioReport(request.getId(), request.getScenarioId(), request.getScenarioName(), ReportTriggerMode.MANUAL.name(), request.getExecuteType(), request.getProjectId(),
                SessionUtils.getUserId());
        // 调用执行方法
        jMeterService.runDefinition(request.getId(), hashTree, request.getReportId(), ApiRunMode.SCENARIO.name());
        return request.getId();
    }

    public ReferenceDTO getReference(ApiScenarioRequest request) {
        ReferenceDTO dto = new ReferenceDTO();
        dto.setScenarioList(extApiScenarioMapper.selectReference(request));
        QueryTestPlanRequest planRequest = new QueryTestPlanRequest();
        planRequest.setScenarioId(request.getId());
        planRequest.setProjectId(request.getProjectId());
        dto.setTestPlanList(extTestPlanMapper.selectTestPlanByRelevancy(planRequest));
        return dto;
    }

    public String addScenarioToPlan(SaveApiPlanRequest request) {
        if (CollectionUtils.isEmpty(request.getPlanIds())) {
            MSException.throwException(Translator.get("plan id is null "));
        }
        List<String> scenarioIds = request.getScenarioIds();
        if (request.isSelectAllDate()) {
            scenarioIds = this.getAllScenarioIdsByFontedSelect(
                    request.getModuleIds(), request.getName(), request.getProjectId(), request.getFilters(), request.getUnSelectIds());
        }
        List<TestPlanDTO> list = extTestPlanMapper.selectByIds(request.getPlanIds());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtTestPlanMapper mapper = sqlSession.getMapper(ExtTestPlanMapper.class);
        ExtTestPlanScenarioCaseMapper scenarioBatchMapper = sqlSession.getMapper(ExtTestPlanScenarioCaseMapper.class);
        ExtTestPlanApiCaseMapper apiCaseBatchMapper = sqlSession.getMapper(ExtTestPlanApiCaseMapper.class);

        for (TestPlanDTO testPlan : list) {
            if (scenarioIds != null) {
                for (String scenarioId : scenarioIds) {
                    TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
                    testPlanApiScenario.setId(UUID.randomUUID().toString());
                    testPlanApiScenario.setApiScenarioId(scenarioId);
                    testPlanApiScenario.setTestPlanId(testPlan.getId());
                    testPlanApiScenario.setCreateTime(System.currentTimeMillis());
                    testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
                    scenarioBatchMapper.insertIfNotExists(testPlanApiScenario);
                }
            }
            if (request.getApiIds() != null) {
                for (String caseId : request.getApiIds()) {
                    TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
                    testPlanApiCase.setId(UUID.randomUUID().toString());
                    testPlanApiCase.setApiCaseId(caseId);
                    testPlanApiCase.setTestPlanId(testPlan.getId());
                    testPlanApiCase.setCreateTime(System.currentTimeMillis());
                    testPlanApiCase.setUpdateTime(System.currentTimeMillis());
                    apiCaseBatchMapper.insertIfNotExists(testPlanApiCase);
                }
            }
        }
        sqlSession.flushStatements();
        return "success";
    }

    public long countScenarioByProjectID(String projectId) {
        return extApiScenarioMapper.countByProjectID(projectId);
    }

    public long countScenarioByProjectIDAndCreatInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());
        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");
        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioMapper.countByProjectIDAndCreatInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countRunResultByProjectID(String projectId) {
        return extApiScenarioMapper.countRunResultByProjectID(projectId);
    }

    public void relevance(ApiCaseRelevanceRequest request) {
        List<String> ids = request.getSelectIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<ApiScenario> apiScenarios = selectByIds(ids);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

        ExtTestPlanScenarioCaseMapper batchMapper = sqlSession.getMapper(ExtTestPlanScenarioCaseMapper.class);
        apiScenarios.forEach(scenario -> {
            TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
            testPlanApiScenario.setId(UUID.randomUUID().toString());
            testPlanApiScenario.setApiScenarioId(scenario.getId());
            testPlanApiScenario.setTestPlanId(request.getPlanId());
            testPlanApiScenario.setCreateTime(System.currentTimeMillis());
            testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
            batchMapper.insertIfNotExists(testPlanApiScenario);
        });
        sqlSession.flushStatements();
    }

    public List<ApiScenario> selectByIds(List<String> ids) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        return apiScenarioMapper.selectByExample(example);
    }

    public void createSchedule(Schedule request) {
        Schedule schedule = scheduleService.buildApiTestSchedule(request);
        schedule.setJob(ApiScenarioTestJob.class.getName());
        schedule.setGroup(ScheduleGroup.API_SCENARIO_TEST.name());
        schedule.setType(ScheduleType.CRON.name());
        scheduleService.addSchedule(schedule);
        this.addOrUpdateApiScenarioCronJob(request);
    }

    public void updateSchedule(Schedule request) {
        scheduleService.editSchedule(request);
        this.addOrUpdateApiScenarioCronJob(request);
    }

    private void addOrUpdateApiScenarioCronJob(Schedule request) {
        scheduleService.addOrUpdateCronJob(
                request, ApiScenarioTestJob.getJobKey(request.getResourceId()), ApiScenarioTestJob.getTriggerKey(request.getResourceId()), ApiScenarioTestJob.class);
    }
}
