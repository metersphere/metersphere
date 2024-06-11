package io.metersphere.plan.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.invoker.GetRunScriptServiceRegister;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.GetRunScriptService;
import io.metersphere.api.service.definition.ApiDefinitionModuleService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.functional.dto.FunctionalCaseModuleCountDTO;
import io.metersphere.functional.dto.ProjectOptionDTO;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.constants.TreeTypeEnums;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanApiCasePageResponse;
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
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
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
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseService extends TestPlanResourceService implements GetRunScriptService {

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private ApiDefinitionModuleService apiDefinitionModuleService;
    private static final String CASE_MODULE_COUNT_ALL = "all";
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private TestPlanResourceLogService testPlanResourceLogService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;
    @Resource
    private ExtTestPlanCollectionMapper extTestPlanCollectionMapper;

    public TestPlanApiCaseService() {
        GetRunScriptServiceRegister.register(ApiExecuteResourceType.TEST_PLAN_API_CASE, this);
    }

    @Override
    public void deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        testPlanApiCaseMapper.deleteByExample(example);
    }

    @Override
    public long getNextOrder(String collectionId) {
        Long maxPos = extTestPlanApiCaseMapper.getMaxPosByCollectionId(collectionId);
        if (maxPos == null) {
            return 0;
        } else {
            return maxPos + DEFAULT_NODE_INTERVAL_POS;
        }
    }

    @Override
    public void updatePos(String id, long pos) {
        extTestPlanApiCaseMapper.updatePos(id, pos);
    }

    @Override
    public Map<String, Long> caseExecResultCount(String testPlanId) {
        List<TestPlanCaseRunResultCount> runResultCounts = extTestPlanApiCaseMapper.selectCaseExecResultCount(testPlanId);
        return runResultCounts.stream().collect(Collectors.toMap(TestPlanCaseRunResultCount::getResult, TestPlanCaseRunResultCount::getResultCount));
    }

    @Override
    public long copyResource(String originalTestPlanId, String newTestPlanId, String operator, long operatorTime) {
        List<TestPlanApiCase> copyList = new ArrayList<>();
        extTestPlanApiCaseMapper.selectByTestPlanIdAndNotDeleted(originalTestPlanId).forEach(originalCase -> {
            TestPlanApiCase newCase = new TestPlanApiCase();
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
        TestPlanApiCaseMapper batchInsertMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
        copyList.forEach(item -> batchInsertMapper.insert(item));
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        return copyList.size();
    }

    @Override
    public void refreshPos(String testPlanId) {
        List<String> caseIdList = extTestPlanApiCaseMapper.selectIdByTestPlanIdOrderByPos(testPlanId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtTestPlanApiCaseMapper batchUpdateMapper = sqlSession.getMapper(ExtTestPlanApiCaseMapper.class);
        for (int i = 0; i < caseIdList.size(); i++) {
            batchUpdateMapper.updatePos(caseIdList.get(i), i * DEFAULT_NODE_INTERVAL_POS);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    /**
     * 获取接口列表
     *
     * @param request
     * @param isRepeat
     * @return
     */
    public List<ApiDefinitionDTO> getApiPage(TestPlanApiRequest request, boolean isRepeat) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new ArrayList<>();
        }
        List<ApiDefinitionDTO> list = extTestPlanApiCaseMapper.list(request, isRepeat);
        apiDefinitionService.processApiDefinitions(list);
        return list;
    }


    /**
     * 获取接口用例列表
     *
     * @param request
     * @param isRepeat
     * @return
     */
    public List<ApiTestCaseDTO> getApiCasePage(TestPlanApiCaseRequest request, boolean isRepeat) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new ArrayList<>();
        }
        return apiTestCaseService.page(request, isRepeat, false, request.getTestPlanId());
    }


    /**
     * 获取已关联的接口用例列表
     *
     * @param request
     * @param deleted
     * @return
     */
    public List<TestPlanApiCasePageResponse> hasRelateApiCaseList(TestPlanApiCaseRequest request, boolean deleted) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new ArrayList<>();
        }
        List<TestPlanApiCasePageResponse> list = extTestPlanApiCaseMapper.relateApiCaseList(request, deleted);
        buildApiCaseResponse(list);
        return list;
    }

    private void buildApiCaseResponse(List<TestPlanApiCasePageResponse> apiCaseList) {
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            Map<String, String> projectMap = getProject(apiCaseList);
            Map<String, String> userMap = getUserMap(apiCaseList);
            handleCaseAndEnv(apiCaseList, projectMap, userMap);
        }
    }

    private void handleCaseAndEnv(List<TestPlanApiCasePageResponse> apiCaseList, Map<String, String> projectMap, Map<String, String> userMap) {
        //获取二级节点环境
        List<TestPlanCollectionEnvDTO> secondEnv = extTestPlanCollectionMapper.selectSecondCollectionEnv(CaseType.API_CASE.getKey(), ModuleConstants.ROOT_NODE_PARENT_ID);
        Map<String, TestPlanCollectionEnvDTO> secondEnvMap = secondEnv.stream().collect(Collectors.toMap(TestPlanCollectionEnvDTO::getId, item -> item));
        //当前用例环境
        List<String> caseEnvIds = apiCaseList.stream().map(TestPlanApiCasePageResponse::getEnvironmentId).toList();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andIdIn(caseEnvIds);
        List<Environment> caseEnv = environmentMapper.selectByExample(environmentExample);
        Map<String, String> caseEnvMap = caseEnv.stream().collect(Collectors.toMap(Environment::getId, Environment::getName));
        apiCaseList.forEach(item -> {
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

    private void doHandleEnv(TestPlanApiCasePageResponse item, TestPlanCollectionEnvDTO collectEnv) {
        if (StringUtils.isNotBlank(collectEnv.getEnvironmentName())) {
            item.setEnvironmentId(collectEnv.getEnvironmentId());
            item.setEnvironmentName(collectEnv.getEnvironmentName());
        } else {
            item.setEnvironmentId(null);
            item.setEnvironmentName(null);
        }
    }

    private void doHandleDefaultEnv(TestPlanApiCasePageResponse item, Map<String, String> caseEnvMap) {
        if (caseEnvMap.containsKey(item.getEnvironmentId())) {
            item.setEnvironmentName(caseEnvMap.get(item.getEnvironmentId()));
        } else {
            item.setEnvironmentId(null);
            item.setEnvironmentName(null);
        }
    }

    private Map<String, String> getUserMap(List<TestPlanApiCasePageResponse> apiCaseList) {
        List<String> userIds = new ArrayList<>();
        userIds.addAll(apiCaseList.stream().map(TestPlanApiCasePageResponse::getCreateUser).toList());
        userIds.addAll(apiCaseList.stream().map(TestPlanApiCasePageResponse::getExecuteUser).toList());
        return userLoginService.getUserNameMap(userIds.stream().filter(StringUtils::isNotBlank).distinct().toList());
    }

    private Map<String, String> getProject(List<TestPlanApiCasePageResponse> apiCaseList) {
        List<String> projectIds = apiCaseList.stream().map(TestPlanApiCasePageResponse::getProjectId).toList();
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);
        List<Project> projectList = projectMapper.selectByExample(projectExample);
        return projectList.stream().collect(Collectors.toMap(Project::getId, Project::getName));
    }


    public Map<String, Long> moduleCount(TestPlanApiCaseModuleRequest request) {
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
     * 已关联接口用例规划视图统计
     *
     * @param request
     * @return
     */
    private Map<String, Long> getCollectionCount(TestPlanApiCaseModuleRequest request) {
        Map<String, Long> projectModuleCountMap = new HashMap<>();
        List<ModuleCountDTO> list = extTestPlanApiCaseMapper.collectionCountByRequest(request.getTestPlanId());
        list.forEach(item -> {
            projectModuleCountMap.put(item.getModuleId(), (long) item.getDataCount());
        });
        long allCount = extTestPlanApiCaseMapper.caseCount(request, false);
        projectModuleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return projectModuleCountMap;
    }

    /**
     * 已关联接口用例模块树统计
     *
     * @param request
     * @return
     */
    private Map<String, Long> getModuleCount(TestPlanApiCaseModuleRequest request) {
        request.setModuleIds(null);
        List<FunctionalCaseModuleCountDTO> projectModuleCountDTOList = extTestPlanApiCaseMapper.countModuleIdByRequest(request, false);
        Map<String, List<FunctionalCaseModuleCountDTO>> projectCountMap = projectModuleCountDTOList.stream().collect(Collectors.groupingBy(FunctionalCaseModuleCountDTO::getProjectId));
        Map<String, Long> projectModuleCountMap = new HashMap<>();
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
        long allCount = extTestPlanApiCaseMapper.caseCount(request, false);
        projectModuleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return projectModuleCountMap;
    }

    public Map<String, Long> getModuleCountMap(String projectId, String testPlanId, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(projectId, testPlanId, moduleCountDTOList);
        //通过广度遍历的方式构建返回值
        return apiDefinitionModuleService.getIdCountMapByBreadth(treeNodeList);
    }


    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, String testPlanId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<String> moduleIds = extTestPlanApiCaseMapper.selectIdByProjectIdAndTestPlanId(projectId, testPlanId);
        List<BaseTreeNode> nodeByNodeIds = apiDefinitionModuleService.getNodeByNodeIds(moduleIds);
        return apiDefinitionModuleService.buildTreeAndCountResource(nodeByNodeIds, moduleCountDTOList, true, Translator.get("functional_case.module.default.name"));
    }

    /**
     * 已关联接口用例模块树
     *
     * @param request
     * @return
     */
    public List<BaseTreeNode> getTree(TestPlanApiCaseTreeRequest request) {
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
        collectionExample.createCriteria().andTypeEqualTo(CaseType.API_CASE.getKey()).andParentIdNotEqualTo(ModuleConstants.ROOT_NODE_PARENT_ID).andTestPlanIdEqualTo(testPlanId);
        List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(collectionExample);
        testPlanCollections.forEach(item -> {
            BaseTreeNode baseTreeNode = new BaseTreeNode(item.getId(), item.getName(), CaseType.API_CASE.getKey());
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
        List<ProjectOptionDTO> rootIds = extTestPlanApiCaseMapper.selectRootIdByTestPlanId(testPlanId);
        Map<String, List<ProjectOptionDTO>> projectRootMap = rootIds.stream().collect(Collectors.groupingBy(ProjectOptionDTO::getName));
        List<ApiCaseModuleDTO> apiCaseModuleIds = extTestPlanApiCaseMapper.selectBaseByProjectIdAndTestPlanId(testPlanId);
        Map<String, List<ApiCaseModuleDTO>> projectModuleMap = apiCaseModuleIds.stream().collect(Collectors.groupingBy(ApiCaseModuleDTO::getProjectId));
        if (MapUtils.isEmpty(projectModuleMap)) {
            projectRootMap.forEach((projectId, projectOptionDTOList) -> {
                BaseTreeNode projectNode = new BaseTreeNode(projectId, projectOptionDTOList.get(0).getProjectName(), Project.class.getName());
                returnList.add(projectNode);
                BaseTreeNode defaultNode = apiDefinitionModuleService.getDefaultModule(Translator.get("functional_case.module.default.name"));
                projectNode.addChild(defaultNode);
            });
            return returnList;
        }
        projectModuleMap.forEach((projectId, moduleList) -> {
            BaseTreeNode projectNode = new BaseTreeNode(projectId, moduleList.get(0).getProjectName(), Project.class.getName());
            returnList.add(projectNode);
            List<String> projectModuleIds = moduleList.stream().map(ApiCaseModuleDTO::getId).toList();
            List<BaseTreeNode> nodeByNodeIds = apiDefinitionModuleService.getNodeByNodeIds(projectModuleIds);
            boolean haveVirtualRootNode = CollectionUtils.isEmpty(projectRootMap.get(projectId));
            List<BaseTreeNode> baseTreeNodes = apiDefinitionModuleService.buildTreeAndCountResource(nodeByNodeIds, !haveVirtualRootNode, Translator.get("functional_case.module.default.name"));
            for (BaseTreeNode baseTreeNode : baseTreeNodes) {
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
    public TestPlanAssociationResponse disassociate(TestPlanApiCaseBatchRequest request, LogInsertModule logInsertModule) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new TestPlanAssociationResponse();
        }
        List<String> selectIds = doSelectIds(request);
        return super.disassociate(
                TestPlanResourceConstants.RESOURCE_API_CASE,
                request,
                logInsertModule,
                selectIds,
                this::deleteTestPlanResource);
    }


    public void deleteTestPlanResource(@Validated TestPlanResourceAssociationParam associationParam) {
        TestPlanApiCaseExample testPlanApiCaseExample = new TestPlanApiCaseExample();
        testPlanApiCaseExample.createCriteria().andIdIn(associationParam.getResourceIdList());
        testPlanApiCaseMapper.deleteByExample(testPlanApiCaseExample);
    }


    public List<String> doSelectIds(TestPlanApiCaseBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extTestPlanApiCaseMapper.getIds(request, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }


    /**
     * 批量更新执行人
     *
     * @param request
     */
    public void batchUpdateExecutor(TestPlanApiCaseUpdateRequest request) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            extTestPlanApiCaseMapper.batchUpdateExecutor(ids, request.getUserId());
        }
    }

    @Override
    public void associateCollection(String planId, Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates, String userId) {
        List<TestPlanApiCase> testPlanApiCaseList = new ArrayList<>();
        //处理数据
        handleApiData(collectionAssociates.get(AssociateCaseType.API), userId, testPlanApiCaseList, planId);
        handleApiCaseData(collectionAssociates.get(AssociateCaseType.API_CASE), userId, testPlanApiCaseList, planId);
        if (CollectionUtils.isNotEmpty(testPlanApiCaseList)) {
            testPlanApiCaseMapper.batchInsert(testPlanApiCaseList);
        }
    }

    private void handleApiCaseData(List<BaseCollectionAssociateRequest> apiCaseList, String userId, List<TestPlanApiCase> testPlanApiCaseList, String planId) {
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            List<String> ids = apiCaseList.stream().flatMap(item -> item.getIds().stream()).toList();
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria().andIdIn(ids);
            List<ApiTestCase> apiTestCaseList = apiTestCaseMapper.selectByExample(example);
            apiCaseList.forEach(apiCase -> {
                List<String> apiCaseIds = apiCase.getIds();
                if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                    List<ApiTestCase> apiTestCases = apiTestCaseList.stream().filter(item -> apiCaseIds.contains(item.getId())).collect(Collectors.toList());
                    buildTestPlanApiCase(planId, apiTestCases, apiCase.getCollectionId(), userId, testPlanApiCaseList);
                }
            });
        }
    }

    private void handleApiData(List<BaseCollectionAssociateRequest> apiCaseList, String userId, List<TestPlanApiCase> testPlanApiCaseList, String planId) {
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            List<String> ids = apiCaseList.stream().flatMap(item -> item.getIds().stream()).toList();
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria().andApiDefinitionIdIn(ids);
            List<ApiTestCase> apiTestCaseList = apiTestCaseMapper.selectByExample(example);
            apiCaseList.forEach(apiCase -> {
                List<String> apiCaseIds = apiCase.getIds();
                if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                    List<ApiTestCase> apiTestCases = apiTestCaseList.stream().filter(item -> apiCaseIds.contains(item.getApiDefinitionId())).collect(Collectors.toList());
                    buildTestPlanApiCase(planId, apiTestCases, apiCase.getCollectionId(), userId, testPlanApiCaseList);
                }
            });
        }
    }

    /**
     * 构建测试计划接口用例对象
     *
     * @param planId
     * @param apiTestCases
     * @param collectionId
     * @param userId
     * @param testPlanApiCaseList
     */
    private void buildTestPlanApiCase(String planId, List<ApiTestCase> apiTestCases, String collectionId, String userId, List<TestPlanApiCase> testPlanApiCaseList) {
        apiTestCases.forEach(apiTestCase -> {
            TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
            testPlanApiCase.setId(IDGenerator.nextStr());
            testPlanApiCase.setTestPlanCollectionId(collectionId);
            testPlanApiCase.setTestPlanId(planId);
            testPlanApiCase.setApiCaseId(apiTestCase.getId());
            testPlanApiCase.setEnvironmentId(apiTestCase.getEnvironmentId());
            testPlanApiCase.setCreateTime(System.currentTimeMillis());
            testPlanApiCase.setCreateUser(userId);
            testPlanApiCase.setPos(getNextOrder(collectionId));
            testPlanApiCaseList.add(testPlanApiCase);
        });
    }

    @Override
    public void initResourceDefaultCollection(String planId, List<TestPlanCollectionDTO> defaultCollections) {
        TestPlanCollectionDTO defaultCollection = defaultCollections.stream().filter(collection -> StringUtils.equals(collection.getType(), CaseType.API_CASE.getKey())
                && !StringUtils.equals(collection.getParentId(), "NONE")).toList().get(0);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanApiCaseMapper apiBatchMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
        TestPlanApiCase record = new TestPlanApiCase();
        record.setTestPlanCollectionId(defaultCollection.getId());
        TestPlanApiCaseExample apiCaseExample = new TestPlanApiCaseExample();
        apiCaseExample.createCriteria().andTestPlanIdEqualTo(planId);
        apiBatchMapper.updateByExampleSelective(record, apiCaseExample);
    }

    public TestPlanOperationResponse sortNode(ResourceSortRequest request, LogInsertModule logInsertModule) {
        TestPlanApiCase dragNode = testPlanApiCaseMapper.selectByPrimaryKey(request.getMoveId());
        if (dragNode == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        TestPlanOperationResponse response = new TestPlanOperationResponse();
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                request.getTestCollectionId(),
                super.getNodeMoveRequest(request, true),
                extTestPlanApiCaseMapper::selectDragInfoById,
                extTestPlanApiCaseMapper::selectNodeByPosOperator
        );
        super.sort(sortDTO);
        response.setOperationCount(1);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(dragNode.getTestPlanId());
        testPlanResourceLogService.saveSortLog(testPlan, request.getMoveId(), new ResourceLogInsertModule(TestPlanResourceConstants.RESOURCE_API_CASE, logInsertModule));
        return response;
    }

    public TaskRequestDTO run(String id, String reportId, String userId) {
        TestPlanApiCase testPlanApiCase = checkResourceExist(id);
        ApiTestCase apiTestCase = apiTestCaseService.checkResourceExist(testPlanApiCase.getApiCaseId());

        String poolId = "todo";
        ApiRunModeConfigDTO runModeConfig = new ApiRunModeConfigDTO();
        // todo 设置 runModeConfig 配置
        TaskRequestDTO taskRequest = getTaskRequest(reportId, id, apiTestCase.getProjectId(), ApiExecuteRunMode.RUN.name());
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

        // 初始化报告
        initApiReport(apiTestCase, testPlanApiCase, reportId, poolId, userId);

        return apiExecuteService.execute(taskRequest);
    }

    public TestPlanApiCase checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(testPlanApiCaseMapper.selectByPrimaryKey(id), "api_test_case_not_exist");
    }

    /**
     * 获取执行脚本
     */
    @Override
    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(taskItem.getResourceId());
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(testPlanApiCase.getApiCaseId());
        return apiTestCaseService.getRunScript(request, apiTestCase);
    }

    /**
     * 预生成用例的执行报告
     *
     * @param apiTestCase
     * @param poolId
     * @param userId
     * @return
     */
    public ApiTestCaseRecord initApiReport(ApiTestCase apiTestCase, TestPlanApiCase testPlanApiCase, String reportId, String poolId, String userId) {
        // 初始化报告
        ApiReport apiReport = apiTestCaseService.getApiReport(apiTestCase, reportId, poolId, userId);
        apiReport.setTestPlanCaseId(testPlanApiCase.getTestPlanId());

        // 创建报告和用例的关联关系
        ApiTestCaseRecord apiTestCaseRecord = apiTestCaseService.getApiTestCaseRecord(apiTestCase, apiReport);

        apiReportService.insertApiReport(List.of(apiReport), List.of(apiTestCaseRecord));

        //初始化步骤
        apiReportService.insertApiReportStep(List.of(getApiReportStep(apiTestCase, reportId)));
        return apiTestCaseRecord;
    }

    public ApiReportStep getApiReportStep(ApiTestCase apiTestCase, String reportId) {
        ApiReportStep apiReportStep = apiTestCaseService.getApiReportStep(apiTestCase, reportId, 1L);
        apiReportStep.setStepType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        return apiReportStep;
    }

    public TaskRequestDTO getTaskRequest(String reportId, String resourceId, String projectId, String runModule) {
        TaskRequestDTO taskRequest = apiTestCaseService.getTaskRequest(reportId, resourceId, projectId, runModule);
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        taskRequest.getTaskInfo().setNeedParseScript(true);
        return taskRequest;
    }
}
