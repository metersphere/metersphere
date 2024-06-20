package io.metersphere.plan.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.dto.scenario.ApiScenarioPageRequest;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiScenarioModuleMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.scenario.ApiScenarioModuleService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.functional.dto.FunctionalCaseModuleCountDTO;
import io.metersphere.functional.dto.ProjectOptionDTO;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.constants.TreeTypeEnums;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanApiScenarioPageResponse;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanOperationResponse;
import io.metersphere.plan.mapper.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiScenarioService extends TestPlanResourceService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanResourceLogService testPlanResourceLogService;
    @Resource
    private ApiScenarioRunService apiScenarioRunService;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private ExtTestPlanCollectionMapper extTestPlanCollectionMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    private static final String CASE_MODULE_COUNT_ALL = "all";
    @Resource
    private ApiScenarioModuleService apiScenarioModuleService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;

    @Override
    public void deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        testPlanApiScenarioMapper.deleteByExample(example);
    }

    @Override
    public long getNextOrder(String collectionId) {
        Long maxPos = extTestPlanApiScenarioMapper.getMaxPosByRangeId(collectionId);
        if (maxPos == null) {
            return DEFAULT_NODE_INTERVAL_POS;
        } else {
            return maxPos + DEFAULT_NODE_INTERVAL_POS;
        }
    }

    @Override
    public void updatePos(String id, long pos) {
        extTestPlanApiScenarioMapper.updatePos(id, pos);
    }

    @Override
    public Map<String, Long> caseExecResultCount(String testPlanId) {
        List<TestPlanCaseRunResultCount> runResultCounts = extTestPlanApiScenarioMapper.selectCaseExecResultCount(testPlanId);
        return runResultCounts.stream().collect(Collectors.toMap(TestPlanCaseRunResultCount::getResult, TestPlanCaseRunResultCount::getResultCount));
    }

    @Override
    public long copyResource(String originalTestPlanId, String newTestPlanId, Map<String, String> oldCollectionIdToNewCollectionId, String operator, long operatorTime) {
        List<TestPlanApiScenario> copyList = new ArrayList<>();
        String defaultCollectionId = extTestPlanCollectionMapper.selectDefaultCollectionId(newTestPlanId, CaseType.SCENARIO_CASE.getKey());
        extTestPlanApiScenarioMapper.selectByTestPlanIdAndNotDeleted(originalTestPlanId).forEach(originalCase -> {
            TestPlanApiScenario newCase = new TestPlanApiScenario();
            BeanUtils.copyBean(newCase, originalCase);
            newCase.setId(IDGenerator.nextStr());
            newCase.setTestPlanId(newTestPlanId);
            newCase.setCreateTime(operatorTime);
            newCase.setCreateUser(operator);
            newCase.setLastExecTime(0L);
            newCase.setTestPlanCollectionId(oldCollectionIdToNewCollectionId.get(newCase.getTestPlanCollectionId()) == null ? defaultCollectionId : oldCollectionIdToNewCollectionId.get(newCase.getTestPlanCollectionId()));
            newCase.setLastExecResult(ExecStatus.PENDING.name());
            newCase.setLastExecReportId(null);
            copyList.add(newCase);
        });

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanApiScenarioMapper batchInsertMapper = sqlSession.getMapper(TestPlanApiScenarioMapper.class);
        copyList.forEach(item -> batchInsertMapper.insert(item));
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        return copyList.size();
    }

    @Override
    public void refreshPos(String testPlanId) {
        List<String> caseIdList = extTestPlanApiScenarioMapper.selectIdByTestPlanIdOrderByPos(testPlanId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtTestPlanApiCaseMapper batchUpdateMapper = sqlSession.getMapper(ExtTestPlanApiCaseMapper.class);
        for (int i = 0; i < caseIdList.size(); i++) {
            batchUpdateMapper.updatePos(caseIdList.get(i), i * DEFAULT_NODE_INTERVAL_POS);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    @Override
    public void associateCollection(String planId, Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates, SessionUser user) {
        List<TestPlanApiScenario> testPlanApiScenarioList = new ArrayList<>();
        List<LogDTO> logDTOS = new ArrayList<>();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        handleApiScenarioData(collectionAssociates.get(AssociateCaseType.API_SCENARIO), user, testPlanApiScenarioList, testPlan, logDTOS);
        if (CollectionUtils.isNotEmpty(testPlanApiScenarioList)) {
            testPlanApiScenarioMapper.batchInsert(testPlanApiScenarioList);
        }
    }

    private void handleApiScenarioData(List<BaseCollectionAssociateRequest> apiScenarioList, SessionUser user, List<TestPlanApiScenario> testPlanApiScenarioList, TestPlan testPlan, List<LogDTO> logDTOS) {
        if (CollectionUtils.isNotEmpty(apiScenarioList)) {
            List<String> ids = apiScenarioList.stream().flatMap(item -> item.getIds().stream()).toList();
            ApiScenarioExample scenarioExample = new ApiScenarioExample();
            scenarioExample.createCriteria().andIdIn(ids);
            List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(scenarioExample);
            apiScenarioList.forEach(apiScenario -> {
                List<String> apiScenarioIds = apiScenario.getIds();
                if (CollectionUtils.isNotEmpty(apiScenarioIds)) {
                    List<ApiScenario> scenarios = apiScenarios.stream().filter(item -> apiScenarioIds.contains(item.getId())).collect(Collectors.toList());
                    buildTestPlanApiScenario(testPlan, scenarios, apiScenario.getCollectionId(), user, testPlanApiScenarioList, logDTOS);
                }
            });
        }
    }

    /**
     * 构建测试计划场景用例对象
     *
     * @param testPlan
     * @param scenarios
     * @param collectionId
     * @param user
     * @param testPlanApiScenarioList
     */
    private void buildTestPlanApiScenario(TestPlan testPlan, List<ApiScenario> scenarios, String collectionId, SessionUser user, List<TestPlanApiScenario> testPlanApiScenarioList, List<LogDTO> logDTOS) {
        super.checkCollection(testPlan.getId(), collectionId, CaseType.SCENARIO_CASE.getKey());
        AtomicLong nextOrder = new AtomicLong(getNextOrder(collectionId));
        scenarios.forEach(scenario -> {
            TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
            testPlanApiScenario.setId(IDGenerator.nextStr());
            testPlanApiScenario.setTestPlanId(testPlan.getId());
            testPlanApiScenario.setApiScenarioId(scenario.getId());
            testPlanApiScenario.setTestPlanCollectionId(collectionId);
            testPlanApiScenario.setGrouped(scenario.getGrouped());
            testPlanApiScenario.setEnvironmentId(scenario.getEnvironmentId());
            testPlanApiScenario.setCreateTime(System.currentTimeMillis());
            testPlanApiScenario.setCreateUser(user.getId());
            testPlanApiScenario.setPos(nextOrder.getAndAdd(DEFAULT_NODE_INTERVAL_POS));
            testPlanApiScenario.setExecuteUser(scenario.getCreateUser());
            testPlanApiScenario.setLastExecResult(ExecStatus.PENDING.name());
            testPlanApiScenarioList.add(testPlanApiScenario);
            buildLog(logDTOS, testPlan, user, scenario);
        });
    }

    private void buildLog(List<LogDTO> logDTOS, TestPlan testPlan, SessionUser user, ApiScenario scenario) {
        LogDTO dto = new LogDTO(
                testPlan.getProjectId(),
                user.getLastOrganizationId(),
                testPlan.getId(),
                user.getId(),
                OperationLogType.ASSOCIATE.name(),
                OperationLogModule.TEST_PLAN,
                Translator.get("log.test_plan.api_scenario") + ":" + scenario.getName());
        dto.setHistory(true);
        dto.setPath("/test-plan/mind/data/edit");
        dto.setMethod(HttpMethodConstants.POST.name());
        logDTOS.add(dto);
    }

    @Override
    public void initResourceDefaultCollection(String planId, List<TestPlanCollectionDTO> defaultCollections) {
        TestPlanCollectionDTO defaultCollection = defaultCollections.stream().filter(collection -> StringUtils.equals(collection.getType(), CaseType.SCENARIO_CASE.getKey())
                && !StringUtils.equals(collection.getParentId(), "NONE")).toList().get(0);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanApiScenarioMapper scenarioBatchMapper = sqlSession.getMapper(TestPlanApiScenarioMapper.class);
        TestPlanApiScenario record = new TestPlanApiScenario();
        record.setTestPlanCollectionId(defaultCollection.getId());
        TestPlanApiScenarioExample scenarioCaseExample = new TestPlanApiScenarioExample();
        scenarioCaseExample.createCriteria().andTestPlanIdEqualTo(planId);
        scenarioBatchMapper.updateByExampleSelective(record, scenarioCaseExample);
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    /**
     * 未关联接口场景列表
     *
     * @param request
     * @param isRepeat
     * @return
     */
    public List<ApiScenarioDTO> getApiScenarioPage(TestPlanApiScenarioRequest request, boolean isRepeat) {
        ApiScenarioPageRequest apiScenarioPageRequest = new ApiScenarioPageRequest();
        BeanUtils.copyBean(apiScenarioPageRequest, request);
        return apiScenarioService.getScenarioPage(apiScenarioPageRequest, isRepeat, request.getTestPlanId());
    }

    public TestPlanOperationResponse sortNode(ResourceSortRequest request, LogInsertModule logInsertModule) {
        TestPlanApiScenario dragNode = testPlanApiScenarioMapper.selectByPrimaryKey(request.getMoveId());
        if (dragNode == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        TestPlanOperationResponse response = new TestPlanOperationResponse();
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                request.getTestCollectionId(),
                super.getNodeMoveRequest(request, false),
                extTestPlanApiScenarioMapper::selectDragInfoById,
                extTestPlanApiScenarioMapper::selectNodeByPosOperator
        );
        super.sort(sortDTO);
        response.setOperationCount(1);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(dragNode.getTestPlanId());
        testPlanResourceLogService.saveSortLog(testPlan, request.getMoveId(), new ResourceLogInsertModule(TestPlanResourceConstants.RESOURCE_API_CASE, logInsertModule));
        return response;
    }


    public TaskRequestDTO run(String id, String reportId, String userId) {
        TestPlanApiScenario testPlanApiScenario = checkResourceExist(id);
        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        testPlanService.setTestPlanUnderway(testPlanApiScenario.getTestPlanId());
        testPlanService.setActualStartTime(testPlanApiScenario.getTestPlanId());
        ApiScenario apiScenario = apiScenarioService.checkResourceExist(testPlanApiScenario.getApiScenarioId());
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(testPlanApiScenario.getTestPlanCollectionId());
        runModeConfig.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanApiScenario.getEnvironmentId()));
        TaskRequestDTO taskRequest = getTaskRequest(reportId, id, apiScenario.getProjectId(), ApiExecuteRunMode.RUN.name());
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        TaskItem taskItem = taskRequest.getTaskItem();
        taskInfo.setRunModeConfig(runModeConfig);
        taskInfo.setSaveResult(true);
        taskInfo.setRealTime(true);
        taskInfo.setUserId(userId);

        if (StringUtils.isEmpty(taskItem.getReportId())) {
            taskInfo.setRealTime(false);
            reportId = IDGenerator.nextStr();
            taskItem.setReportId(reportId);
        } else {
            // 如果传了报告ID，则实时获取结果
            taskInfo.setRealTime(true);
        }

        ApiScenarioReport scenarioReport = apiScenarioRunService.getScenarioReport(userId);
        scenarioReport.setId(reportId);
        scenarioReport.setTriggerMode(TaskTriggerMode.MANUAL.name());
        scenarioReport.setRunMode(ApiBatchRunMode.PARALLEL.name());
        scenarioReport.setPoolId(runModeConfig.getPoolId());
        scenarioReport.setEnvironmentId(runModeConfig.getEnvironmentId());
        scenarioReport.setTestPlanScenarioId(testPlanApiScenario.getId());
        apiScenarioRunService.initApiReport(apiScenario, scenarioReport);

        return apiExecuteService.execute(taskRequest);
    }

    public TestPlanApiScenario checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(testPlanApiScenarioMapper.selectByPrimaryKey(id), "permission.system_api_scenario.name");
    }

    public TaskRequestDTO getTaskRequest(String reportId, String resourceId, String projectId, String runModule) {
        TaskRequestDTO taskRequest = apiScenarioRunService.getTaskRequest(reportId, resourceId, projectId, runModule);
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name());
        taskRequest.getTaskInfo().setNeedParseScript(true);
        return taskRequest;
    }

    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(taskItem.getResourceId());
        ApiScenarioDetail apiScenarioDetail = apiScenarioRunService.getForRun(testPlanApiScenario.getApiScenarioId());
        apiScenarioDetail.setEnvironmentId(testPlanApiScenario.getEnvironmentId());
        apiScenarioDetail.setGrouped(testPlanApiScenario.getGrouped());
        return apiScenarioRunService.getRunScript(request, testPlanApiScenario.getApiScenarioId());
    }


    /**
     * 已关联接口场景列表
     *
     * @param request
     * @param deleted
     * @return
     */
    public List<TestPlanApiScenarioPageResponse> hasRelateApiScenarioList(TestPlanApiScenarioRequest request, boolean deleted) {
        List<TestPlanApiScenarioPageResponse> list = extTestPlanApiScenarioMapper.relateApiScenarioList(request, deleted);
        buildApiScenarioResponse(list, request.getTestPlanId());
        return list;
    }

    private void buildApiScenarioResponse(List<TestPlanApiScenarioPageResponse> apiScenarioList, String testPlanId) {
        if (CollectionUtils.isNotEmpty(apiScenarioList)) {
            Map<String, String> projectMap = getProject(apiScenarioList);
            Map<String, String> userMap = getUserMap(apiScenarioList);
            Map<String, String> moduleNameMap = getModuleName(apiScenarioList);
            handleScenarioAndEnv(apiScenarioList, projectMap, userMap, testPlanId, moduleNameMap);
        }
    }

    private Map<String, String> getModuleName(List<TestPlanApiScenarioPageResponse> apiScenarioList) {
        List<String> moduleIds = apiScenarioList.stream().map(TestPlanApiScenarioPageResponse::getModuleId).distinct().toList();
        ApiScenarioModuleExample moduleExample = new ApiScenarioModuleExample();
        moduleExample.createCriteria().andIdIn(moduleIds);
        List<ApiScenarioModule> modules = apiScenarioModuleMapper.selectByExample(moduleExample);
        Map<String, String> moduleNameMap = modules.stream().collect(Collectors.toMap(ApiScenarioModule::getId, ApiScenarioModule::getName));
        return moduleNameMap;
    }

    private void handleScenarioAndEnv(List<TestPlanApiScenarioPageResponse> apiScenarioList, Map<String, String> projectMap, Map<String, String> userMap, String testPlanId, Map<String, String> moduleNameMap) {
        //获取二级节点环境
        List<TestPlanCollectionEnvDTO> secondEnv = extTestPlanCollectionMapper.selectSecondCollectionEnv(CaseType.SCENARIO_CASE.getKey(), ModuleConstants.ROOT_NODE_PARENT_ID, testPlanId);
        Map<String, TestPlanCollectionEnvDTO> secondEnvMap = secondEnv.stream().collect(Collectors.toMap(TestPlanCollectionEnvDTO::getId, item -> item));
        //获取一级节点环境
        TestPlanCollectionEnvDTO firstEnv = extTestPlanCollectionMapper.selectFirstCollectionEnv(CaseType.SCENARIO_CASE.getKey(), ModuleConstants.ROOT_NODE_PARENT_ID, testPlanId);
        //当前用例环境
        List<String> caseEnvIds = apiScenarioList.stream().map(TestPlanApiScenarioPageResponse::getEnvironmentId).toList();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andIdIn(caseEnvIds);
        List<Environment> caseEnv = environmentMapper.selectByExample(environmentExample);
        Map<String, String> caseEnvMap = caseEnv.stream().collect(Collectors.toMap(Environment::getId, Environment::getName));
        apiScenarioList.forEach(item -> {
            item.setProjectName(projectMap.get(item.getProjectId()));
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setExecuteUserName(userMap.get(item.getExecuteUser()));
            item.setModuleName(StringUtils.isNotBlank(moduleNameMap.get(item.getModuleId())) ? moduleNameMap.get(item.getModuleId()) : Translator.get("api_unplanned_scenario"));
            if (secondEnvMap.containsKey(item.getTestPlanCollectionId())) {
                TestPlanCollectionEnvDTO collectEnv = secondEnvMap.get(item.getTestPlanCollectionId());
                if (collectEnv.getExtended()) {
                    //继承
                    getRunEnv(firstEnv, caseEnvMap, item);
                } else {
                    getRunEnv(collectEnv, caseEnvMap, item);
                }
            }
        });
    }

    private void getRunEnv(TestPlanCollectionEnvDTO collectEnv, Map<String, String> caseEnvMap, TestPlanApiScenarioPageResponse item) {
        if (StringUtils.equalsIgnoreCase(collectEnv.getEnvironmentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            //计划集 == 默认环境   处理默认环境
            doHandleDefaultEnv(item, caseEnvMap);
        } else {
            //计划集 != 默认环境
            doHandleEnv(item, collectEnv);
        }
    }

    private void doHandleEnv(TestPlanApiScenarioPageResponse item, TestPlanCollectionEnvDTO collectEnv) {
        if (StringUtils.isNotBlank(collectEnv.getEnvironmentName())) {
            item.setEnvironmentId(collectEnv.getEnvironmentId());
            item.setEnvironmentName(collectEnv.getEnvironmentName());
        } else {
            item.setEnvironmentId(null);
            item.setEnvironmentName(null);
        }
    }

    private void doHandleDefaultEnv(TestPlanApiScenarioPageResponse item, Map<String, String> caseEnvMap) {
        if (caseEnvMap.containsKey(item.getEnvironmentId())) {
            item.setEnvironmentName(caseEnvMap.get(item.getEnvironmentId()));
        } else {
            item.setEnvironmentId(null);
            item.setEnvironmentName(null);
        }
    }

    private Map<String, String> getUserMap(List<TestPlanApiScenarioPageResponse> apiScenarioList) {
        List<String> userIds = new ArrayList<>();
        userIds.addAll(apiScenarioList.stream().map(TestPlanApiScenarioPageResponse::getCreateUser).toList());
        userIds.addAll(apiScenarioList.stream().map(TestPlanApiScenarioPageResponse::getExecuteUser).toList());
        return userLoginService.getUserNameMap(userIds.stream().filter(StringUtils::isNotBlank).distinct().toList());
    }


    private Map<String, String> getProject(List<TestPlanApiScenarioPageResponse> apiScenarioList) {
        List<String> projectIds = apiScenarioList.stream().map(TestPlanApiScenarioPageResponse::getProjectId).toList();
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);
        List<Project> projectList = projectMapper.selectByExample(projectExample);
        return projectList.stream().collect(Collectors.toMap(Project::getId, Project::getName));
    }

    public Map<String, Long> moduleCount(TestPlanApiScenarioModuleRequest request) {
        switch (request.getTreeType()) {
            case TreeTypeEnums.MODULE:
                return getModuleCount(request);
            case TreeTypeEnums.COLLECTION:
                return getCollectionCount(request);
            default:
                return new HashMap<>();
        }
    }

    /**
     * 已关联场景 规划视图统计
     *
     * @param request
     * @return
     */
    private Map<String, Long> getCollectionCount(TestPlanApiScenarioModuleRequest request) {
        request.setCollectionId(null);
        Map<String, Long> projectModuleCountMap = new HashMap<>();
        List<ModuleCountDTO> list = extTestPlanApiScenarioMapper.collectionCountByRequest(request);
        list.forEach(item -> {
            projectModuleCountMap.put(item.getModuleId(), (long) item.getDataCount());
        });
        long allCount = extTestPlanApiScenarioMapper.caseCount(request, false);
        projectModuleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return projectModuleCountMap;
    }

    /**
     * 已关联场景 模块树统计
     *
     * @param request
     * @return
     */
    private Map<String, Long> getModuleCount(TestPlanApiScenarioModuleRequest request) {
        request.setModuleIds(null);
        List<FunctionalCaseModuleCountDTO> projectModuleCountDTOList = extTestPlanApiScenarioMapper.countModuleIdByRequest(request, false);
        Map<String, List<FunctionalCaseModuleCountDTO>> projectCountMap = projectModuleCountDTOList.stream().collect(Collectors.groupingBy(FunctionalCaseModuleCountDTO::getProjectId));
        Map<String, Long> projectModuleCountMap = projectModuleCountDTOList.stream()
                .collect(Collectors.groupingBy(
                        FunctionalCaseModuleCountDTO::getModuleId,
                        Collectors.summingLong(FunctionalCaseModuleCountDTO::getDataCount)));

        projectCountMap.forEach((projectId, moduleCountDTOList) -> {
            List<ModuleCountDTO> moduleCountDTOS = new ArrayList<>();
            for (FunctionalCaseModuleCountDTO functionalCaseModuleCountDTO : moduleCountDTOList) {
                ModuleCountDTO moduleCountDTO = new ModuleCountDTO();
                BeanUtils.copyBean(moduleCountDTO, functionalCaseModuleCountDTO);
                moduleCountDTOS.add(moduleCountDTO);
            }
            int sum = moduleCountDTOList.stream().mapToInt(FunctionalCaseModuleCountDTO::getDataCount).sum();
            Map<String, Long> moduleCountMap = getModuleCountMap(projectId, request.getTestPlanId(), moduleCountDTOS);
            moduleCountMap.forEach((k, v) -> {
                if (projectModuleCountMap.get(k) == null || projectModuleCountMap.get(k) == 0L) {
                    projectModuleCountMap.put(k, v);
                }
            });
            projectModuleCountMap.put(projectId, (long) sum);
        });
        //查出全部用例数量
        long allCount = extTestPlanApiScenarioMapper.caseCount(request, false);
        projectModuleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return projectModuleCountMap;
    }

    private Map<String, Long> getModuleCountMap(String projectId, String testPlanId, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(projectId, testPlanId, moduleCountDTOList);
        //通过广度遍历的方式构建返回值
        return apiScenarioModuleService.getIdCountMapByBreadth(treeNodeList);
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, String testPlanId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<String> moduleIds = extTestPlanApiScenarioMapper.selectIdByProjectIdAndTestPlanId(projectId, testPlanId);
        List<BaseTreeNode> nodeByNodeIds = apiScenarioModuleService.getNodeByNodeIds(moduleIds);
        return apiScenarioModuleService.buildTreeAndCountResource(nodeByNodeIds, moduleCountDTOList, true, Translator.get("functional_case.module.default.name"));
    }

    public List<BaseTreeNode> getTree(TestPlanTreeRequest request) {
        switch (request.getTreeType()) {
            case TreeTypeEnums.MODULE:
                return getModuleTree(request.getTestPlanId());
            case TreeTypeEnums.COLLECTION:
                return getCollectionTree(request.getTestPlanId());
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 已关联接口用例规划视图树
     *
     * @param testPlanId
     * @return
     */
    private List<BaseTreeNode> getCollectionTree(String testPlanId) {
        List<BaseTreeNode> returnList = new ArrayList<>();
        TestPlanCollectionExample collectionExample = new TestPlanCollectionExample();
        collectionExample.createCriteria().andTypeEqualTo(CaseType.SCENARIO_CASE.getKey()).andParentIdNotEqualTo(ModuleConstants.ROOT_NODE_PARENT_ID).andTestPlanIdEqualTo(testPlanId);
        List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(collectionExample);
        testPlanCollections.forEach(item -> {
            BaseTreeNode baseTreeNode = new BaseTreeNode(item.getId(), Translator.get(item.getName(), item.getName()), CaseType.SCENARIO_CASE.getKey());
            returnList.add(baseTreeNode);
        });
        return returnList;
    }

    /**
     * 模块树
     *
     * @param testPlanId
     * @return
     */
    private List<BaseTreeNode> getModuleTree(String testPlanId) {
        List<BaseTreeNode> returnList = new ArrayList<>();

        List<ProjectOptionDTO> moduleLists = extTestPlanApiScenarioMapper.selectRootIdByTestPlanId(testPlanId);
        // 获取所有的项目id
        List<String> projectIds = moduleLists.stream().map(ProjectOptionDTO::getName).distinct().toList();
        // moduleLists中id=root的数据
        List<ProjectOptionDTO> rootModuleList = moduleLists.stream().filter(item -> StringUtils.equals(item.getId(), ModuleConstants.DEFAULT_NODE_ID)).toList();
        Map<String, List<ProjectOptionDTO>> projectRootMap = rootModuleList.stream().collect(Collectors.groupingBy(ProjectOptionDTO::getName));
        List<ApiScenarioModuleDTO> apiCaseModuleIds = extTestPlanApiScenarioMapper.selectBaseByProjectIdAndTestPlanId(testPlanId);
        Map<String, List<ApiScenarioModuleDTO>> projectModuleMap = apiCaseModuleIds.stream().collect(Collectors.groupingBy(ApiScenarioModuleDTO::getProjectId));
        projectIds.forEach(projectId -> {
            boolean needCreateRoot = MapUtils.isNotEmpty(projectRootMap) && projectRootMap.containsKey(projectId);
            boolean needCreateModule = MapUtils.isNotEmpty(projectModuleMap) && projectModuleMap.containsKey(projectId);
            // 项目名称是
            String projectName = needCreateModule ? projectModuleMap.get(projectId).getFirst().getProjectName() : projectRootMap.get(projectId).getFirst().getProjectName();
            // 构建项目那一层级
            BaseTreeNode projectNode = new BaseTreeNode(projectId, projectName, "PROJECT");
            returnList.add(projectNode);
            List<BaseTreeNode> nodeByNodeIds = new ArrayList<>();
            if (needCreateModule) {
                List<String> projectModuleIds = projectModuleMap.get(projectId).stream().map(ApiScenarioModuleDTO::getId).toList();
                nodeByNodeIds = apiScenarioModuleService.getNodeByNodeIds(projectModuleIds);
            }
            List<BaseTreeNode> baseTreeNodes = apiScenarioModuleService.buildTreeAndCountResource(nodeByNodeIds, needCreateRoot, Translator.get("api_unplanned_scenario"));
            for (BaseTreeNode baseTreeNode : baseTreeNodes) {
                if (StringUtils.equals(baseTreeNode.getId(), ModuleConstants.DEFAULT_NODE_ID)) {
                    // 默认拼项目id
                    baseTreeNode.setId(projectId + "_" + ModuleConstants.DEFAULT_NODE_ID);
                }
                projectNode.addChild(baseTreeNode);
            }
        });
        return returnList;
    }


    /**
     * 取消关联
     *
     * @param request
     * @param logInsertModule
     * @return
     */
    public TestPlanAssociationResponse disassociate(BasePlanCaseBatchRequest request, LogInsertModule logInsertModule) {
        List<String> selectIds = doSelectIds(request);
        return super.disassociate(
                TestPlanResourceConstants.RESOURCE_API_SCENARIO,
                request,
                logInsertModule,
                selectIds,
                this::deleteTestPlanResource);
    }


    public void deleteTestPlanResource(@Validated TestPlanResourceAssociationParam associationParam) {
        TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
        testPlanApiScenarioExample.createCriteria().andIdIn(associationParam.getResourceIdList());
        testPlanApiScenarioMapper.deleteByExample(testPlanApiScenarioExample);
    }

    public List<String> doSelectIds(BasePlanCaseBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extTestPlanApiScenarioMapper.getIds(request, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public void batchUpdateExecutor(TestPlanApiScenarioUpdateRequest request) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            extTestPlanApiScenarioMapper.batchUpdateExecutor(ids, request.getUserId());
        }
    }

    public void checkReportIsTestPlan(String id) {
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andIdEqualTo(id).andTestPlanScenarioIdNotEqualTo("NONE");
        if (apiScenarioReportMapper.countByExample(example) == 0) {
            throw new MSException(Translator.get("api_scenario_report_not_exist"));
        }

    }


    /**
     * 批量移动
     *
     * @param request
     */
    public void batchMove(BaseBatchMoveRequest request) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            moveCaseToCollection(ids, request.getTargetCollectionId());
        }
    }

    private void moveCaseToCollection(List<String> ids, String targetCollectionId) {
        AtomicLong nextOrder = new AtomicLong(getNextOrder(targetCollectionId));
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanApiScenarioMapper testPlanApiScenarioMapper = sqlSession.getMapper(TestPlanApiScenarioMapper.class);
        ids.forEach(id -> {
            TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
            testPlanApiScenario.setId(id);
            testPlanApiScenario.setPos(nextOrder.getAndAdd(DEFAULT_NODE_INTERVAL_POS));
            testPlanApiScenario.setTestPlanCollectionId(targetCollectionId);
            testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public List<TestPlanApiScenario> getSelectIdAndCollectionId(TestPlanApiScenarioBatchRunRequest request) {
        if (request.isSelectAll()) {
            List<TestPlanApiScenario> testPlanApiCases = extTestPlanApiScenarioMapper.getSelectIdAndCollectionId(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                testPlanApiCases.removeAll(request.getExcludeIds());
            }
            return testPlanApiCases;
        } else {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andIdIn(request.getSelectIds());
            Map<String, TestPlanApiScenario> testPlanApiScenarioMap = testPlanApiScenarioMapper.selectByExample(example)
                    .stream()
                    .collect(Collectors.toMap(TestPlanApiScenario::getId, Function.identity()));
            List<TestPlanApiScenario> testPlanApiScenarios = new ArrayList<>(request.getSelectIds().size());
            // 按ID的顺序排序
            for (String id : request.getSelectIds()) {
                TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMap.get(id);
                if (testPlanApiScenario != null) {
                    testPlanApiScenarios.add(testPlanApiScenario);
                }
            }
            return testPlanApiScenarios;
        }
    }
}
