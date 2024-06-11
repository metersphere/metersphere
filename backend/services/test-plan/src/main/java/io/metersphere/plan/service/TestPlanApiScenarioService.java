package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.invoker.GetRunScriptServiceRegister;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.GetRunScriptService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.domain.TestPlanApiScenarioExample;
import io.metersphere.plan.dto.ResourceLogInsertModule;
import io.metersphere.plan.dto.TestPlanCaseRunResultCount;
import io.metersphere.plan.dto.TestPlanCollectionDTO;
import io.metersphere.plan.dto.TestPlanCollectionEnvDTO;
import io.metersphere.plan.dto.request.BaseCollectionAssociateRequest;
import io.metersphere.plan.dto.request.ResourceSortRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioRequest;
import io.metersphere.plan.dto.response.TestPlanApiScenarioPageResponse;
import io.metersphere.plan.dto.response.TestPlanOperationResponse;
import io.metersphere.plan.mapper.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiScenarioService extends TestPlanResourceService implements GetRunScriptService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private TestPlanMapper testPlanMapper;
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

    public TestPlanApiScenarioService() {
        GetRunScriptServiceRegister.register(ApiExecuteResourceType.TEST_PLAN_API_SCENARIO, this);
    }

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
            return 0;
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
    public long copyResource(String originalTestPlanId, String newTestPlanId, String operator, long operatorTime) {
        List<TestPlanApiScenario> copyList = new ArrayList<>();
        extTestPlanApiScenarioMapper.selectByTestPlanIdAndNotDeleted(originalTestPlanId).forEach(originalCase -> {
            TestPlanApiScenario newCase = new TestPlanApiScenario();
            BeanUtils.copyBean(newCase, originalCase);
            newCase.setId(IDGenerator.nextStr());
            newCase.setTestPlanId(newTestPlanId);
            newCase.setCreateTime(operatorTime);
            newCase.setCreateUser(operator);
            newCase.setLastExecTime(0L);
            newCase.setLastExecResult(null);
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
    public void associateCollection(String planId, Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates, String userId) {
        List<BaseCollectionAssociateRequest> apiScenarios = collectionAssociates.get(AssociateCaseType.API_SCENARIO);
        // TODO: 调用具体的关联场景用例入库方法  入参{计划ID, 测试集ID, 关联的用例ID集合}
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
    }

    /**
     * 未关联接口场景列表
     *
     * @param request
     * @param isRepeat
     * @return
     */
    public List<ApiScenarioDTO> getApiScenarioPage(TestPlanApiScenarioRequest request, boolean isRepeat) {
        List<ApiScenarioDTO> scenarioPage = apiScenarioService.getScenarioPage(request, isRepeat, request.getTestPlanId());
        return scenarioPage;
    }

    public TestPlanOperationResponse sortNode(ResourceSortRequest request, LogInsertModule logInsertModule) {
        TestPlanApiScenario dragNode = testPlanApiScenarioMapper.selectByPrimaryKey(request.getMoveId());
        if (dragNode == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        TestPlanOperationResponse response = new TestPlanOperationResponse();
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                request.getTestCollectionId(),
                super.getNodeMoveRequest(request, true),
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
        ApiScenario apiScenario = apiScenarioService.checkResourceExist(testPlanApiScenario.getApiScenarioId());

        String poolId = "todo";
        String envId = "todo";
        ApiRunModeConfigDTO runModeConfig = new ApiRunModeConfigDTO();
        // todo 设置 runModeConfig 配置
        TaskRequestDTO taskRequest = getTaskRequest(reportId, id, apiScenario.getProjectId(), ApiExecuteRunMode.RUN.name());
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        TaskItem taskItem = taskRequest.getTaskItem();
        taskInfo.setRunModeConfig(runModeConfig);
        taskInfo.setSaveResult(true);
        taskInfo.setRealTime(true);

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
        scenarioReport.setPoolId(poolId);
        scenarioReport.setEnvironmentId(envId);
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

    @Override
    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(taskItem.getResourceId());
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
        buildApiScenarioResponse(list);
        return list;
    }

    private void buildApiScenarioResponse(List<TestPlanApiScenarioPageResponse> apiScenarioList) {
        if (CollectionUtils.isNotEmpty(apiScenarioList)) {
            Map<String, String> projectMap = getProject(apiScenarioList);
            Map<String, String> userMap = getUserMap(apiScenarioList);
            handleScenarioAndEnv(apiScenarioList, projectMap, userMap);
        }
    }

    private void handleScenarioAndEnv(List<TestPlanApiScenarioPageResponse> apiScenarioList, Map<String, String> projectMap, Map<String, String> userMap) {
        //获取二级节点环境
        List<TestPlanCollectionEnvDTO> secondEnv = extTestPlanCollectionMapper.selectSecondCollectionEnv(CaseType.SCENARIO_CASE.getKey(), ModuleConstants.ROOT_NODE_PARENT_ID);
        Map<String, TestPlanCollectionEnvDTO> secondEnvMap = secondEnv.stream().collect(Collectors.toMap(TestPlanCollectionEnvDTO::getId, item -> item));
        //当前用例环境
        List<String> caseEnvIds = apiScenarioList.stream().map(TestPlanApiScenarioPageResponse::getEnvironmentId).toList();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andIdIn(caseEnvIds);
        List<Environment> caseEnv = environmentMapper.selectByExample(environmentExample);
        Map<String, String> caseEnvMap = caseEnv.stream().collect(Collectors.toMap(Environment::getId, Environment::getName));
        apiScenarioList.forEach(item -> {
            item.setProjectName(projectMap.get(item.getProjectId()));
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            TestPlanCollectionEnvDTO collectEnv = secondEnvMap.get(item.getTestPlanCollectionId());
            if (StringUtils.equalsIgnoreCase(collectEnv.getEnvironmentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                //计划集 == 默认环境   处理默认环境
                doHandleDefaultEnv(item, caseEnvMap);
            } else {
                //计划集 != 默认环境
                doHandleEnv(item, collectEnv);
            }
        });
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
}
