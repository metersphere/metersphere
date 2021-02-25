package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.JmxInfoDTO;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.automation.parse.ScenarioImport;
import io.metersphere.api.dto.automation.parse.ScenarioImportParserFactory;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.dto.definition.request.unknown.MsJmeterElement;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.parse.ApiImportParser;
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
import io.metersphere.commons.utils.*;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.ApiScenarioTestJob;
import io.metersphere.job.sechedule.TestPlanTestJob;
import io.metersphere.service.ScheduleService;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.FileOperationRequest;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiAutomationService {
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private APITestService apiTestService;
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
    @Resource
    @Lazy
    private TestPlanScenarioCaseService testPlanScenarioCaseService;

    public List<ApiScenarioDTO> list(ApiScenarioRequest request) {
        request = this.initRequest(request, true, true);
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(request);
        return list;
    }

    /**
     * 初始化部分参数
     *
     * @param request
     * @param setDefultOrders
     * @param checkThisWeekData
     * @return
     */
    private ApiScenarioRequest initRequest(ApiScenarioRequest request, boolean setDefultOrders, boolean checkThisWeekData) {
        if (setDefultOrders) {
            request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        }
        if (StringUtils.isNotEmpty(request.getExecuteStatus())) {
            Map<String, List<String>> statusFilter = new HashMap<>();
            List<String> list = new ArrayList<>();
            list.add("Prepare");
            list.add("Underway");
            list.add("Completed");
            statusFilter.put("status", list);
            request.setFilters(statusFilter);
        }
        if (checkThisWeekData) {
            if (request.isSelectThisWeedData()) {
                Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
                Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
                if (weekFirstTime != null) {
                    request.setCreateTime(weekFirstTime.getTime());
                }
            }
        }
        return request;
    }


    public List<String> selectIdsNotExistsInPlan(String projectId, String planId) {
        return extApiScenarioMapper.selectIdsNotExistsInPlan(projectId, planId);
    }

    public void deleteByIds(List<String> nodeIds) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andApiScenarioModuleIdIn(nodeIds);
        apiScenarioMapper.deleteByExample(example);
    }

    public void removeToGcByIds(List<String> nodeIds) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andApiScenarioModuleIdIn(nodeIds);
        extApiScenarioMapper.removeToGcByExample(example);
    }

    public ApiScenario create(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles) {
        request.setId(UUID.randomUUID().toString());
        checkNameExist(request);

        final ApiScenarioWithBLOBs scenario = buildSaveScenario(request);

        scenario.setCreateTime(System.currentTimeMillis());
        scenario.setNum(getNextNum(request.getProjectId()));

        apiScenarioMapper.insert(scenario);

        List<String> bodyUploadIds = request.getBodyUploadIds();
        FileUtils.createBodyFiles(bodyUploadIds, bodyFiles);
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
        FileUtils.createBodyFiles(bodyUploadIds, bodyFiles);

        final ApiScenarioWithBLOBs scenario = buildSaveScenario(request);
        apiScenarioMapper.updateByPrimaryKeySelective(scenario);
    }

    public ApiScenarioWithBLOBs buildSaveScenario(SaveApiScenarioRequest request) {
        ApiScenarioWithBLOBs scenario = new ApiScenarioWithBLOBs();
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
        scenario.setUpdateTime(System.currentTimeMillis());
        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));
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
        return scenario;
    }

    public void delete(String id) {
        //及连删除外键表
        this.preDelete(id);
        testPlanScenarioCaseService.deleteByScenarioId(id);
        apiScenarioMapper.deleteByPrimaryKey(id);
    }

    public void preDelete(String scenarioId) {
        List<String> ids = new ArrayList<>();
        ids.add(scenarioId);
        deleteApiScenarioReport(ids);

        scheduleService.deleteScheduleAndJobByResourceId(scenarioId, ScheduleGroup.API_SCENARIO_TEST.name());
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
        //将这些场景的定时任务删除掉
        for (String id : apiIds) {
            scheduleService.deleteScheduleAndJobByResourceId(id, ScheduleGroup.API_SCENARIO_TEST.name());
        }
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

    public byte[] loadFileAsBytes(FileOperationRequest fileOperationRequest) {
        File file = new File("/opt/metersphere/data/body/" + fileOperationRequest.getId() + "_" + fileOperationRequest.getName());
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);) {
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            return bos.toByteArray();
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
        }
        return null;
    }

    public void createScenarioReport(String id, String scenarioId, String scenarioName, String triggerMode, String execType, String projectId, String userID) {
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
     * 生成HashTree
     *
     * @param apiScenarios 场景
     * @param request      请求参数
     * @param reportIds    报告ID
     * @return hashTree
     */
    private HashTree generateHashTree(List<ApiScenarioWithBLOBs> apiScenarios, RunScenarioRequest request, List<String> reportIds) {
        HashTree jmeterHashTree = new ListedHashTree();
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        try {
            boolean isFirst = true;
            for (ApiScenarioWithBLOBs item : apiScenarios) {
                if (item.getStepTotal() == null || item.getStepTotal() == 0) {
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
                            new TypeReference<LinkedList<MsTestElement>>() {
                            });
                    scenario.setHashTree(elements);
                }
                if (StringUtils.isNotEmpty(element.getString("variables"))) {
                    LinkedList<ScenarioVariable> variables = mapper.readValue(element.getString("variables"),
                            new TypeReference<LinkedList<ScenarioVariable>>() {
                            });
                    scenario.setVariables(variables);
                }
                group.setEnableCookieShare(scenario.isEnableCookieShare());
                LinkedList<MsTestElement> scenarios = new LinkedList<>();
                scenarios.add(scenario);
                // 创建场景报告
                if (reportIds != null) {
                    //如果是测试计划页面触发的执行方式，生成报告时createScenarioReport第二个参数需要特殊处理
                    if (StringUtils.equals(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
                        String testPlanScenarioId = item.getId();
                        if (request.getScenarioTestPlanIdMap() != null && request.getScenarioTestPlanIdMap().containsKey(item.getId())) {
                            testPlanScenarioId = request.getScenarioTestPlanIdMap().get(item.getId());
                        }
                        createScenarioReport(group.getName(), testPlanScenarioId, item.getName(), request.getTriggerMode() == null ? ReportTriggerMode.MANUAL.name() : request.getTriggerMode(),
                                request.getExecuteType(), item.getProjectId(), request.getReportUserID());
                    } else {
                        createScenarioReport(group.getName(), item.getId(), item.getName(), request.getTriggerMode() == null ? ReportTriggerMode.MANUAL.name() : request.getTriggerMode(),
                                request.getExecuteType(), item.getProjectId(), request.getReportUserID());
                    }

                    reportIds.add(group.getName());
                }
                group.setHashTree(scenarios);
                testPlan.getHashTree().add(group);
            }
        } catch (Exception ex) {
            MSException.throwException(ex.getMessage());
        }

        testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());
        return jmeterHashTree;
    }

    private String generateJmx(ApiScenarioWithBLOBs apiScenario) {
        HashTree jmeterHashTree = new ListedHashTree();
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setName(apiScenario.getName());
        testPlan.setHashTree(new LinkedList<>());
        ParameterConfig config = new ParameterConfig();
        config.setOperating(true);
        try {

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JSONObject element = JSON.parseObject(apiScenario.getScenarioDefinition());
            MsScenario scenario = JSONObject.parseObject(apiScenario.getScenarioDefinition(), MsScenario.class);

            // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
            if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                        new TypeReference<LinkedList<MsTestElement>>() {
                        });
                scenario.setHashTree(elements);
            }
            if (element != null && StringUtils.isNotEmpty(element.getString("variables"))) {
                LinkedList<ScenarioVariable> variables = mapper.readValue(element.getString("variables"),
                        new TypeReference<LinkedList<ScenarioVariable>>() {
                        });
                scenario.setVariables(variables);
            }
            if (scenario == null) {
                return null;
            }
            // 针对导入的jmx 处理
            if (CollectionUtils.isNotEmpty(scenario.getHashTree()) && (scenario.getHashTree().get(0) instanceof MsJmeterElement)) {
                scenario.toHashTree(jmeterHashTree, scenario.getHashTree(), config);
                return scenario.getJmx(jmeterHashTree);
            } else {
                MsThreadGroup group = new MsThreadGroup();
                group.setLabel(apiScenario.getName());
                group.setName(apiScenario.getName());
                group.setEnableCookieShare(scenario.isEnableCookieShare());
                group.setHashTree(new LinkedList<MsTestElement>() {{
                    this.add(scenario);
                }});
                testPlan.getHashTree().add(group);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            MSException.throwException(ex.getMessage());
        }
        testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), config);
        return testPlan.getJmx(jmeterHashTree);
    }

    /**
     * 场景测试执行
     *
     * @param request
     * @return
     */
    public String run(RunScenarioRequest request) {

        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));

        List<String> ids = request.getIds();
        //检查是否有正在执行中的情景
        this.checkScenarioIsRunning(ids);
        List<ApiScenarioWithBLOBs> apiScenarios = extApiScenarioMapper.selectIds(ids);

        String runMode = ApiRunMode.SCENARIO.name();
        if (StringUtils.isNotBlank(request.getRunMode()) && StringUtils.equals(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
            runMode = ApiRunMode.SCENARIO_PLAN.name();
        }
        if (StringUtils.isNotBlank(request.getRunMode()) && StringUtils.equals(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
            runMode = ApiRunMode.DEFINITION.name();
        }
        // 调用执行方法
        List<String> reportIds = new LinkedList<>();
        HashTree hashTree = generateHashTree(apiScenarios, request, reportIds);
        jMeterService.runDefinition(JSON.toJSONString(reportIds), hashTree, request.getReportId(), runMode);
        return request.getId();
    }

    public void checkScenarioIsRunning(List<String> ids) {
        List<ApiScenarioReport> lastReportStatusByIds = apiReportService.selectLastReportByIds(ids);
        for (ApiScenarioReport report : lastReportStatusByIds) {
            if (StringUtils.equals(report.getStatus(), APITestStatus.Running.name())) {
                MSException.throwException(report.getName() + " Is Running!");
            }
        }
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
    private List<String> getAllScenarioIdsByFontedSelect(List<String> moduleIds, String name, String projectId, Map<String, List<String>> filters, List<String> unSelectIds) {
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
        FileUtils.createBodyFiles(bodyUploadIds, bodyFiles);
        Map<String,EnvironmentConfig> envConfig = new HashMap<>();
        Map<String, String> map = request.getEnvironmentMap();
        if (map != null) {
            map.keySet().forEach(id -> {
                ApiTestEnvironmentWithBLOBs environment = environmentService.get(map.get(id));
                EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                envConfig.put(id, env);
            });
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

    public List<ApiScenarioWithBLOBs> selectByIdsWithBLOBs(List<String> ids) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        return apiScenarioMapper.selectByExampleWithBLOBs(example);
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
        if (StringUtils.equals(request.getGroup(), ScheduleGroup.TEST_PLAN_TEST.name())) {
            scheduleService.addOrUpdateCronJob(
                    request, TestPlanTestJob.getJobKey(request.getResourceId()), TestPlanTestJob.getTriggerKey(request.getResourceId()), TestPlanTestJob.class);
        } else {
            scheduleService.addOrUpdateCronJob(
                    request, ApiScenarioTestJob.getJobKey(request.getResourceId()), ApiScenarioTestJob.getTriggerKey(request.getResourceId()), ApiScenarioTestJob.class);
        }

    }

    public JmxInfoDTO genPerformanceTestJmx(RunScenarioRequest request) throws Exception {
        List<ApiScenarioWithBLOBs> apiScenarios = null;
        List<String> ids = request.getIds();
        apiScenarios = extApiScenarioMapper.selectIds(ids);
        String testName = "";
        if (!apiScenarios.isEmpty()) {
            testName = apiScenarios.get(0).getName();
        }
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());

        HashTree jmeterHashTree = generateHashTree(apiScenarios, request, null);
        String jmx = testPlan.getJmx(jmeterHashTree);

        jmx = apiTestService.updateJmxString(jmx, testName, true);

        //将ThreadGroup的testname改为接口名称
//        Document doc = DocumentHelper.parseText(jmx);// 获取可续保保单列表报文模板
//        Element root = doc.getRootElement();
//        Element rootHashTreeElement = root.element("hashTree");
//        Element innerHashTreeElement = rootHashTreeElement.elements("hashTree").get(0);
//        Element theadGroupElement = innerHashTreeElement.elements("ThreadGroup").get(0);
//        theadGroupElement.attribute("testname").setText(testName);
//        jmx = root.asXML();

        String name = request.getName() + ".jmx";

        JmxInfoDTO dto = new JmxInfoDTO();
        dto.setName(name);
        dto.setXml(jmx);
        return dto;
    }

    public void bathEdit(ApiScenarioBatchRequest request) {

        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));

        if (StringUtils.isNotBlank(request.getEnvironmentId())) {
            bathEditEnv(request);
            return;
        }
        ApiScenarioExample apiScenarioExample = new ApiScenarioExample();
        apiScenarioExample.createCriteria().andIdIn(request.getIds());
        ApiScenarioWithBLOBs apiScenarioWithBLOBs = new ApiScenarioWithBLOBs();
        BeanUtils.copyBean(apiScenarioWithBLOBs, request);
        apiScenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        apiScenarioMapper.updateByExampleSelective(
                apiScenarioWithBLOBs,
                apiScenarioExample);
    }

    public void bathEditEnv(ApiScenarioBatchRequest request) {
        if (StringUtils.isNotBlank(request.getEnvironmentId())) {
            List<ApiScenarioWithBLOBs> apiScenarios = selectByIdsWithBLOBs(request.getIds());
            apiScenarios.forEach(item -> {
                JSONObject object = JSONObject.parseObject(item.getScenarioDefinition());
                object.put("environmentId", request.getEnvironmentId());
                if (object != null) {
                    item.setScenarioDefinition(JSONObject.toJSONString(object));
                }
                apiScenarioMapper.updateByPrimaryKeySelective(item);
            });
        }
    }

    public List<ApiScenarioWithBLOBs> getWithBLOBs(ApiScenarioWithBLOBs request) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andStatusNotEqualTo("Trash").andIdNotEqualTo(request.getId());
        return apiScenarioMapper.selectByExampleWithBLOBs(example);
    }

    private void _importCreate(List<ApiScenarioWithBLOBs> sameRequest, ApiScenarioMapper batchMapper, ApiScenarioWithBLOBs scenarioWithBLOBs, ApiTestImportRequest apiTestImportRequest) {
        if (CollectionUtils.isEmpty(sameRequest)) {
            scenarioWithBLOBs.setId(UUID.randomUUID().toString());
            batchMapper.insert(scenarioWithBLOBs);
        } else {
            //如果存在则修改
            scenarioWithBLOBs.setId(sameRequest.get(0).getId());
            batchMapper.updateByPrimaryKeyWithBLOBs(scenarioWithBLOBs);
        }
    }

    private ApiScenarioWithBLOBs importCreate(ApiScenarioWithBLOBs request, ApiScenarioMapper batchMapper, ApiTestImportRequest apiTestImportRequest) {
        final ApiScenarioWithBLOBs scenarioWithBLOBs = new ApiScenarioWithBLOBs();
        BeanUtils.copyBean(scenarioWithBLOBs, request);
        scenarioWithBLOBs.setCreateTime(System.currentTimeMillis());
        scenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        scenarioWithBLOBs.setStatus(APITestStatus.Underway.name());
        scenarioWithBLOBs.setProjectId(apiTestImportRequest.getProjectId());
        if (StringUtils.isEmpty(request.getPrincipal())) {
            scenarioWithBLOBs.setPrincipal(Objects.requireNonNull(SessionUtils.getUser()).getId());
        }
        if (request.getUserId() == null) {
            scenarioWithBLOBs.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        } else {
            scenarioWithBLOBs.setUserId(request.getUserId());
        }
        scenarioWithBLOBs.setDescription(request.getDescription());

        List<ApiScenarioWithBLOBs> sameRequest = getWithBLOBs(scenarioWithBLOBs);
        if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            _importCreate(sameRequest, batchMapper, scenarioWithBLOBs, apiTestImportRequest);
        } else if (StringUtils.equals("incrementalMerge", apiTestImportRequest.getModeId())) {
            if (CollectionUtils.isEmpty(sameRequest)) {
                batchMapper.insert(scenarioWithBLOBs);
            }

        } else {
            _importCreate(sameRequest, batchMapper, scenarioWithBLOBs, apiTestImportRequest);
        }
        return scenarioWithBLOBs;
    }

    private void editScenario(ApiTestImportRequest request, ScenarioImport apiImport) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper batchMapper = sqlSession.getMapper(ApiScenarioMapper.class);
        List<ApiScenarioWithBLOBs> data = apiImport.getData();
        int num = 0;
        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            num = getNextNum(data.get(0).getProjectId());
        }
        for (int i = 0; i < data.size(); i++) {
            ApiScenarioWithBLOBs item = data.get(i);
            if (item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            item.setNum(num++);
            importCreate(item, batchMapper, request);
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
    }

    public ScenarioImport scenarioImport(MultipartFile file, ApiTestImportRequest request) {
        ApiImportParser apiImportParser = ScenarioImportParserFactory.getImportParser(request.getPlatform());
        ScenarioImport apiImport = null;
        Optional.ofNullable(file)
                .ifPresent(item -> request.setFileName(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."))));
        try {
            apiImport = (ScenarioImport) Objects.requireNonNull(apiImportParser).parse(file == null ? null : file.getInputStream(), request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("parse_data_error"));
        }
        if (apiImport != null) {
            editScenario(request, apiImport);
        }
        return apiImport;
    }

    private List<ApiScenarioWithBLOBs> getExportResult(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(request.getIds());
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = apiScenarioMapper.selectByExampleWithBLOBs(example);
        return apiScenarioWithBLOBs;
    }

    public ApiScenrioExportResult export(ApiScenarioBatchRequest request) {
        ApiScenrioExportResult result = new ApiScenrioExportResult();
        result.setData(getExportResult(request));
        result.setProjectId(request.getProjectId());
        result.setVersion(System.getenv("MS_VERSION"));
        return result;
    }

    public List<ApiScenrioExportJmx> exportJmx(ApiScenarioBatchRequest request) {
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = getExportResult(request);
        // 生成jmx
        List<ApiScenrioExportJmx> resList = new ArrayList<>();
        apiScenarioWithBLOBs.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                String jmx = generateJmx(item);
                if (StringUtils.isNotEmpty(jmx)) {
                    ApiScenrioExportJmx scenrioExportJmx = new ApiScenrioExportJmx(item.getName(), apiTestService.updateJmxString(jmx, null, true));
                    resList.add(scenrioExportJmx);
                }
            }
        });
        return resList;
    }

}
