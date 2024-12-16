package io.metersphere.plan.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ApiModuleRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCasePageRequest;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.mapper.ExtApiDefinitionModuleMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.definition.*;
import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.domain.BugRelationCaseExample;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
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
import io.metersphere.sdk.dto.AssociateCaseDTO;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.ModuleSelectDTO;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseService extends TestPlanResourceService {

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
    private ApiTestCaseRunService apiTestCaseRunService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;
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
    @Resource
    private TestPlanConfigService testPlanConfigService;
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ExtApiDefinitionModuleMapper extApiDefinitionModuleMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    private static final String DEBUG_MODULE_COUNT_ALL = "all";
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private BaseTaskHubService baseTaskHubService;

    private static final String EXECUTOR = "executeUserName";

    @Resource
    private BugRelationCaseMapper bugRelationCaseMapper;

    @Override
    public List<TestPlanResourceExecResultDTO> selectDistinctExecResultByProjectId(String projectId) {
        return extTestPlanApiCaseMapper.selectDistinctExecResult(projectId);
    }

    @Override
    public List<TestPlanResourceExecResultDTO> selectDistinctExecResultByTestPlanIds(List<String> testPlanIds) {
        return extTestPlanApiCaseMapper.selectDistinctExecResultByTestPlanIds(testPlanIds);
    }

    @Override
    public List<TestPlanResourceExecResultDTO> selectLastExecResultByTestPlanIds(List<String> testPlanIds) {
        return extTestPlanApiCaseMapper.selectLastExecResultByTestPlanIds(testPlanIds);
    }

    @Override
    public List<TestPlanResourceExecResultDTO> selectLastExecResultByProjectId(String projectId) {
        return extTestPlanApiCaseMapper.selectLastExecResultByProjectId(projectId);
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
            return DEFAULT_NODE_INTERVAL_POS;
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
    public long copyResource(String originalTestPlanId, String newTestPlanId, Map<String, String> oldCollectionIdToNewCollectionId, String operator, long operatorTime) {
        List<TestPlanApiCase> copyList = new ArrayList<>();
        String defaultCollectionId = extTestPlanCollectionMapper.selectDefaultCollectionId(newTestPlanId, CaseType.SCENARIO_CASE.getKey());
        extTestPlanApiCaseMapper.selectByTestPlanIdAndNotDeleted(originalTestPlanId).forEach(originalCase -> {
            TestPlanApiCase newCase = new TestPlanApiCase();
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
        ApiTestCasePageRequest pageRequest = new ApiTestCasePageRequest();
        BeanUtils.copyBean(pageRequest, request);
        return apiTestCaseService.page(pageRequest, false, isRepeat, request.getTestPlanId());
    }


    /**
     * 获取已关联的接口用例列表
     *
     * @param request
     * @param deleted
     * @return
     */
    public List<TestPlanApiCasePageResponse> hasRelateApiCaseList(TestPlanApiCaseRequest request, boolean deleted, String currentProjectId) {
        request.setNullExecutorKey(filterCaseRequest(request.getFilter()));
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new ArrayList<>();
        }
        List<TestPlanApiCasePageResponse> list = extTestPlanApiCaseMapper.relateApiCaseList(request, deleted);
        buildApiCaseResponse(list, request.getTestPlanId(), currentProjectId);
        return list;
    }

    private void buildApiCaseResponse(List<TestPlanApiCasePageResponse> apiCaseList, String testPlanId, String projectId) {
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            Map<String, String> projectMap = getProject(apiCaseList);
            Map<String, String> userMap = getUserMap(apiCaseList);
            Map<String, String> moduleNameMap = getModuleName(apiCaseList);
            List<String> associateIds = apiCaseList.stream().map(TestPlanApiCasePageResponse::getId).toList();
            Map<String, List<TestPlanCaseBugDTO>> associateBugMap = queryCaseAssociateBug(associateIds, projectId);
            handleCaseAndEnv(apiCaseList, projectMap, userMap, testPlanId, moduleNameMap, associateBugMap);
        }
    }

    private Map<String, String> getModuleName(List<TestPlanApiCasePageResponse> apiCaseList) {
        List<String> moduleIds = apiCaseList.stream().map(TestPlanApiCasePageResponse::getModuleId).toList();
        List<ApiDefinitionModule> modules = extApiDefinitionModuleMapper.getNameInfoByIds(moduleIds);
        return modules.stream()
                .collect(Collectors.toMap(ApiDefinitionModule::getId, ApiDefinitionModule::getName));
    }

    private void handleCaseAndEnv(List<TestPlanApiCasePageResponse> apiCaseList, Map<String, String> projectMap, Map<String, String> userMap,
                                  String testPlanId, Map<String, String> moduleNameMap, Map<String, List<TestPlanCaseBugDTO>> associateBugMap) {
        //获取二级节点环境
        List<TestPlanCollectionEnvDTO> secondEnv = extTestPlanCollectionMapper.selectSecondCollectionEnv(CaseType.API_CASE.getKey(), ModuleConstants.ROOT_NODE_PARENT_ID, testPlanId);
        Map<String, TestPlanCollectionEnvDTO> secondEnvMap = secondEnv.stream().collect(Collectors.toMap(TestPlanCollectionEnvDTO::getId, item -> item));
        //获取一级节点环境
        TestPlanCollectionEnvDTO firstEnv = extTestPlanCollectionMapper.selectFirstCollectionEnv(CaseType.API_CASE.getKey(), ModuleConstants.ROOT_NODE_PARENT_ID, testPlanId);
        //当前用例环境
        List<String> caseEnvIds = apiCaseList.stream().map(TestPlanApiCasePageResponse::getEnvironmentId).toList();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andIdIn(caseEnvIds);
        List<Environment> caseEnv = environmentMapper.selectByExample(environmentExample);
        Map<String, String> caseEnvMap = caseEnv.stream().collect(Collectors.toMap(Environment::getId, Environment::getName));

        apiCaseList.forEach(item -> {
            item.setProjectName(projectMap.get(item.getProjectId()));
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setExecuteUserName(userMap.get(item.getExecuteUser()));
            item.setModuleName(StringUtils.isNotBlank(moduleNameMap.get(item.getModuleId())) ? moduleNameMap.get(item.getModuleId()) : Translator.get("api_unplanned_request"));
            if (secondEnvMap.containsKey(item.getTestPlanCollectionId())) {
                TestPlanCollectionEnvDTO collectEnv = secondEnvMap.get(item.getTestPlanCollectionId());
                if (collectEnv.getExtended()) {
                    //继承
                    getRunEnv(firstEnv, caseEnvMap, item);
                } else {
                    getRunEnv(collectEnv, caseEnvMap, item);
                }
            }
            if (associateBugMap.containsKey(item.getId())) {
                List<TestPlanCaseBugDTO> associateBugs = associateBugMap.get(item.getId());
                item.setBugList(associateBugs);
                item.setBugCount(associateBugs.size());
            }
        });
    }

    private void getRunEnv(TestPlanCollectionEnvDTO collectEnv, Map<String, String> caseEnvMap, TestPlanApiCasePageResponse item) {
        if (StringUtils.equalsIgnoreCase(collectEnv.getEnvironmentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            //计划集 == 默认环境   处理默认环境
            doHandleDefaultEnv(item, caseEnvMap);
        } else {
            //计划集 != 默认环境
            doHandleEnv(item, collectEnv);
        }
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
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return Collections.emptyMap();
        }
        request.setNullExecutorKey(filterCaseRequest(request.getFilter()));
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
        request.setCollectionId(null);
        Map<String, Long> projectModuleCountMap = new HashMap<>();
        List<ModuleCountDTO> list = extTestPlanApiCaseMapper.collectionCountByRequest(request);
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
        //projectModuleCountDTOList转新的map key 是moduleId value是数量 stream实现
        Map<String, Long> projectModuleCountMap = projectModuleCountDTOList.stream()
                .filter(item -> StringUtils.equals(item.getModuleId(), item.getProjectId() + "_" + ModuleConstants.DEFAULT_NODE_ID))
                .collect(Collectors.groupingBy(FunctionalCaseModuleCountDTO::getModuleId, Collectors.summingLong(FunctionalCaseModuleCountDTO::getDataCount)));
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
        collectionExample.setOrderByClause("pos asc");
        List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(collectionExample);
        testPlanCollections.forEach(item -> {
            BaseTreeNode baseTreeNode = new BaseTreeNode(item.getId(), Translator.get(item.getName(), item.getName()), CaseType.API_CASE.getKey());
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
        List<ProjectOptionDTO> moduleLists = extTestPlanApiCaseMapper.selectRootIdByTestPlanId(testPlanId);
        // 获取所有的项目id
        List<String> projectIds = moduleLists.stream().map(ProjectOptionDTO::getName).distinct().toList();
        // moduleLists中id=root的数据
        List<ProjectOptionDTO> rootModuleList = moduleLists.stream().filter(item -> StringUtils.equals(item.getId(), ModuleConstants.DEFAULT_NODE_ID)).toList();

        Map<String, List<ProjectOptionDTO>> projectRootMap = rootModuleList.stream().collect(Collectors.groupingBy(ProjectOptionDTO::getName));
        List<ApiCaseModuleDTO> apiCaseModuleIds = extTestPlanApiCaseMapper.selectBaseByProjectIdAndTestPlanId(testPlanId);
        Map<String, List<ApiCaseModuleDTO>> projectModuleMap = apiCaseModuleIds.stream().collect(Collectors.groupingBy(ApiCaseModuleDTO::getProjectId));
        projectIds.forEach(projectId -> {
            // 如果projectRootMap中没有projectId，说明该项目没有根节点 不需要创建
            // projectModuleMap中没有projectId，说明该项目没有模块 不需要创建
            // 如果都有  需要创建完整的数结构
            boolean needCreateRoot = MapUtils.isNotEmpty(projectRootMap) && projectRootMap.containsKey(projectId);
            boolean needCreateModule = MapUtils.isNotEmpty(projectModuleMap) && projectModuleMap.containsKey(projectId);
            // 项目名称是
            String projectName = needCreateModule ? projectModuleMap.get(projectId).getFirst().getProjectName() : projectRootMap.get(projectId).getFirst().getProjectName();
            // 构建项目那一层级
            BaseTreeNode projectNode = new BaseTreeNode(projectId, projectName, "PROJECT");
            returnList.add(projectNode);
            List<BaseTreeNode> nodeByNodeIds = new ArrayList<>();
            if (needCreateModule) {
                List<String> projectModuleIds = projectModuleMap.get(projectId).stream().map(ApiCaseModuleDTO::getId).toList();
                nodeByNodeIds = apiDefinitionModuleService.getNodeByNodeIds(projectModuleIds);
            }
            List<BaseTreeNode> baseTreeNodes = apiDefinitionModuleService.buildTreeAndCountResource(nodeByNodeIds, needCreateRoot, Translator.get("api_unplanned_request"));
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
    public TestPlanAssociationResponse disassociate(TestPlanApiCaseBatchRequest request, LogInsertModule logInsertModule) {
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
            request.setNullExecutorKey(filterCaseRequest(request.getCondition().getFilter()));
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
    public void associateCollection(String planId, Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates, SessionUser user) {
        List<TestPlanApiCase> testPlanApiCaseList = new ArrayList<>();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        boolean isRepeat = testPlanConfigService.isRepeatCase(testPlan.getId());
        //处理数据
        handleApiData(collectionAssociates.get(AssociateCaseType.API), user, testPlanApiCaseList, testPlan, isRepeat);
        handleApiCaseData(collectionAssociates.get(AssociateCaseType.API_CASE), user, testPlanApiCaseList, testPlan, isRepeat);

        if (CollectionUtils.isNotEmpty(testPlanApiCaseList)) {
            List<TestPlanApiCase> distinctList = testPlanApiCaseList.stream()
                    .collect(Collectors.toMap(
                            TestPlanApiCase::getApiCaseId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ))
                    .values().stream()
                    .toList();
            testPlanApiCaseMapper.batchInsert(distinctList);
        }
    }

    private void handleApiCaseData(List<BaseCollectionAssociateRequest> apiCaseList, SessionUser user, List<TestPlanApiCase> testPlanApiCaseList, TestPlan testPlan, boolean isRepeat) {
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            apiCaseList.forEach(apiCase -> {
                super.checkCollection(testPlan.getId(), apiCase.getCollectionId(), CaseType.API_CASE.getKey());
                boolean selectAllModule = apiCase.getModules().isSelectAllModule();
                Map<String, ModuleSelectDTO> moduleMaps = apiCase.getModules().getModuleMaps();
                moduleMaps.remove(MODULE_ALL);
                if (selectAllModule) {
                    // 选择了全部模块
                    List<ApiTestCase> apiTestCaseList = extApiTestCaseMapper.selectAllApiCase(isRepeat, apiCase.getModules().getProjectId(), testPlan.getId(), apiCase.getModules().getProtocols());
                    buildTestPlanApiCaseDTO(apiCase.getCollectionId(), apiTestCaseList, testPlan, user, testPlanApiCaseList);
                } else {
                    AssociateCaseDTO dto = super.getCaseIds(moduleMaps);
                    List<ApiTestCase> apiTestCaseList = new ArrayList<>();
                    //获取全选的模块数据
                    if (CollectionUtils.isNotEmpty(dto.getModuleIds())) {
                        apiTestCaseList = extApiTestCaseMapper.getListBySelectModules(isRepeat, apiCase.getModules().getProjectId(), dto.getModuleIds(), testPlan.getId(), apiCase.getModules().getProtocols());
                    }

                    if (CollectionUtils.isNotEmpty(dto.getSelectIds())) {
                        CollectionUtils.removeAll(dto.getSelectIds(), apiTestCaseList.stream().map(ApiTestCase::getId).toList());
                        //获取选中的ids数据
                        List<ApiTestCase> selectIdList = extApiTestCaseMapper.getListBySelectIds(apiCase.getModules().getProjectId(), dto.getSelectIds(), testPlan.getId(), apiCase.getModules().getProtocols());
                        apiTestCaseList.addAll(selectIdList);
                    }

                    if (CollectionUtils.isNotEmpty(dto.getExcludeIds())) {
                        //排除的ids
                        List<String> excludeIds = dto.getExcludeIds();
                        apiTestCaseList = apiTestCaseList.stream().filter(item -> !excludeIds.contains(item.getId())).toList();
                    }

                    if (CollectionUtils.isNotEmpty(apiTestCaseList)) {
                        List<ApiTestCase> list = apiTestCaseList.stream().sorted(Comparator.comparing(ApiTestCase::getPos)).toList();
                        buildTestPlanApiCaseDTO(apiCase.getCollectionId(), list, testPlan, user, testPlanApiCaseList);
                    }

                }
            });
        }
    }


    private void handleApiData(List<BaseCollectionAssociateRequest> apiCaseList, SessionUser user, List<TestPlanApiCase> testPlanApiCaseList, TestPlan testPlan, boolean isRepeat) {
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            apiCaseList.forEach(apiCase -> {
                super.checkCollection(testPlan.getId(), apiCase.getCollectionId(), CaseType.API_CASE.getKey());
                boolean selectAllModule = apiCase.getModules().isSelectAllModule();
                Map<String, ModuleSelectDTO> moduleMaps = apiCase.getModules().getModuleMaps();
                moduleMaps.remove(MODULE_ALL);
                if (selectAllModule) {
                    // 选择了全部模块
                    List<ApiTestCase> apiTestCaseList = extApiTestCaseMapper.selectAllApiCase(isRepeat, apiCase.getModules().getProjectId(), testPlan.getId(), apiCase.getModules().getProtocols());
                    buildTestPlanApiCaseDTO(apiCase.getCollectionId(), apiTestCaseList, testPlan, user, testPlanApiCaseList);
                } else {
                    AssociateCaseDTO dto = super.getCaseIds(moduleMaps);
                    List<ApiTestCase> apiTestCaseList = new ArrayList<>();
                    //获取全选的模块数据
                    if (CollectionUtils.isNotEmpty(dto.getModuleIds())) {
                        apiTestCaseList = extApiTestCaseMapper.getListBySelectModules(isRepeat, apiCase.getModules().getProjectId(), dto.getModuleIds(), testPlan.getId(), apiCase.getModules().getProtocols());
                    }

                    if (CollectionUtils.isNotEmpty(dto.getSelectIds())) {
                        //获取选中的ids数据
                        List<ApiTestCase> selectIdList = extApiTestCaseMapper.getCaseListBySelectIds(isRepeat, apiCase.getModules().getProjectId(), dto.getSelectIds(), testPlan.getId(), apiCase.getModules().getProtocols());
                        apiTestCaseList.addAll(selectIdList);
                    }

                    if (CollectionUtils.isNotEmpty(dto.getExcludeIds())) {
                        //排除的ids
                        List<ApiTestCase> excludeList = extApiTestCaseMapper.getCaseListBySelectIds(isRepeat, apiCase.getModules().getProjectId(), dto.getExcludeIds(), testPlan.getId(), apiCase.getModules().getProtocols());
                        List<String> excludeIds = excludeList.stream().map(ApiTestCase::getId).toList();
                        apiTestCaseList = apiTestCaseList.stream().filter(item -> !excludeIds.contains(item.getId())).distinct().toList();
                    }

                    if (CollectionUtils.isNotEmpty(apiTestCaseList)) {
                        List<ApiTestCase> list = apiTestCaseList.stream().sorted(Comparator.comparing(ApiTestCase::getPos)).toList();
                        buildTestPlanApiCaseDTO(apiCase.getCollectionId(), list, testPlan, user, testPlanApiCaseList);
                    }

                }
            });
        }
    }


    /**
     * 构建测试计划接口用例对象
     *
     * @param collectionId
     * @param apiTestCaseList
     * @param testPlan
     * @param user
     * @param testPlanApiCaseList
     */
    public void buildTestPlanApiCaseDTO(String collectionId, List<ApiTestCase> apiTestCaseList, TestPlan testPlan, SessionUser user, List<TestPlanApiCase> testPlanApiCaseList) {
        AtomicLong nextOrder = new AtomicLong(getNextOrder(collectionId));
        apiTestCaseList.forEach(apiTestCase -> {
            TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
            testPlanApiCase.setId(IDGenerator.nextStr());
            testPlanApiCase.setTestPlanCollectionId(collectionId);
            testPlanApiCase.setTestPlanId(testPlan.getId());
            testPlanApiCase.setApiCaseId(apiTestCase.getId());
            testPlanApiCase.setEnvironmentId(apiTestCase.getEnvironmentId());
            testPlanApiCase.setCreateTime(System.currentTimeMillis());
            testPlanApiCase.setCreateUser(user.getId());
            testPlanApiCase.setPos(nextOrder.getAndAdd(DEFAULT_NODE_INTERVAL_POS));
            testPlanApiCase.setLastExecResult(ExecStatus.PENDING.name());
            testPlanApiCaseList.add(testPlanApiCase);
        });
    }

    @Override
    public void initResourceDefaultCollection(String planId, List<TestPlanCollectionDTO> defaultCollections) {
        TestPlanCollectionDTO defaultCollection = defaultCollections.stream().filter(collection -> StringUtils.equals(collection.getType(), CaseType.API_CASE.getKey())
                && !StringUtils.equals(collection.getParentId(), "NONE")).toList().getFirst();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanApiCaseMapper apiBatchMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
        TestPlanApiCase record = new TestPlanApiCase();
        record.setTestPlanCollectionId(defaultCollection.getId());
        TestPlanApiCaseExample apiCaseExample = new TestPlanApiCaseExample();
        apiCaseExample.createCriteria().andTestPlanIdEqualTo(planId);
        apiBatchMapper.updateByExampleSelective(record, apiCaseExample);
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public TestPlanOperationResponse sortNode(ResourceSortRequest request, LogInsertModule logInsertModule) {
        TestPlanApiCase dragNode = testPlanApiCaseMapper.selectByPrimaryKey(request.getMoveId());
        if (dragNode == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        TestPlanOperationResponse response = new TestPlanOperationResponse();
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                request.getTestCollectionId(),
                super.getNodeMoveRequest(request, false),
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
        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        testPlanService.setActualStartTime(testPlanApiCase.getTestPlanId());
        ApiTestCase apiTestCase = apiTestCaseService.checkResourceExist(testPlanApiCase.getApiCaseId());
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(testPlanApiCase.getTestPlanCollectionId());
        runModeConfig.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanApiCase.getEnvironmentId()));
        runModeConfig.setRunMode(ApiBatchRunMode.PARALLEL.name());
        TaskRequestDTO taskRequest = getTaskRequest(reportId, id, apiTestCase.getProjectId(), ApiExecuteRunMode.RUN.name());

        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());

        ExecTask execTask = apiCommonService.newExecTask(project.getId(), userId);
        execTask.setCaseCount(1L);
        execTask.setTaskName(apiTestCase.getName());
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(TaskTriggerMode.MANUAL.name());
        execTask.setTaskType(ExecTaskType.TEST_PLAN_API_CASE.name());

        ExecTaskItem execTaskItem = apiCommonService.newExecTaskItem(execTask.getId(), project.getId(), userId);
        execTaskItem.setOrganizationId(project.getOrganizationId());
        execTaskItem.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        execTaskItem.setResourceId(testPlanApiCase.getId());
        execTaskItem.setCaseId(apiTestCase.getId());
        execTaskItem.setTaskOrigin(testPlanApiCase.getTestPlanId());
        execTaskItem.setResourceName(apiTestCase.getName());

        baseTaskHubService.insertExecTaskAndDetail(execTask, execTaskItem);

        TaskInfo taskInfo = taskRequest.getTaskInfo();
        TaskItem taskItem = taskRequest.getTaskItem();
        taskInfo.setTaskId(execTask.getId());
        taskInfo.setRunModeConfig(runModeConfig);
        taskInfo.setSaveResult(true);
        taskInfo.setRealTime(true);
        taskInfo.setUserId(userId);
        taskItem.setId(execTaskItem.getId());

        if (StringUtils.isEmpty(taskItem.getReportId())) {
            taskInfo.setRealTime(false);
        } else {
            // 如果传了报告ID，则实时获取结果
            taskInfo.setRealTime(true);
        }

        return apiExecuteService.executePlanResource(taskRequest);
    }

    public void runRun(ExecTask execTask, ExecTaskItem execTaskItem, String userId) {
        String id = execTaskItem.getResourceId();
        TestPlanApiCase testPlanApiCase = checkResourceExist(id);
        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        testPlanService.setActualStartTime(testPlanApiCase.getTestPlanId());
        ApiTestCase apiTestCase = apiTestCaseService.checkResourceExist(testPlanApiCase.getApiCaseId());
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(testPlanApiCase.getTestPlanCollectionId());
        runModeConfig.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanApiCase.getEnvironmentId()));
        runModeConfig.setRunMode(ApiBatchRunMode.PARALLEL.name());
        TaskRequestDTO taskRequest = getTaskRequest(null, id, apiTestCase.getProjectId(), ApiExecuteRunMode.RUN.name());

        TaskInfo taskInfo = taskRequest.getTaskInfo();
        TaskItem taskItem = taskRequest.getTaskItem();
        taskInfo.setTaskId(execTask.getId());
        taskInfo.setRunModeConfig(runModeConfig);
        taskInfo.setSaveResult(true);
        taskInfo.setRealTime(true);
        taskInfo.setUserId(userId);
        taskItem.setId(execTaskItem.getId());
        taskInfo.setRealTime(false);

        apiExecuteService.execute(taskRequest);
    }

    public TestPlanApiCase checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(testPlanApiCaseMapper.selectByPrimaryKey(id), "api_test_case_not_exist");
    }

    /**
     * 预生成用例的执行报告
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String initApiReport(ApiTestCase apiTestCase, TestPlanApiCase testPlanApiCase, GetRunScriptRequest request) {
        // 初始化报告
        ApiRunModeConfigDTO runModeConfig = request.getRunModeConfig();
        ApiReport apiReport = apiTestCaseRunService.getApiReport(apiTestCase, request);
        apiReport.setEnvironmentId(apiTestCaseRunService.getEnvId(runModeConfig, testPlanApiCase.getEnvironmentId()));
        apiReport.setTestPlanCaseId(testPlanApiCase.getId());
        apiReportService.insertApiReport(apiReport);

        // 创建报告和用例的关联关系
        ApiTestCaseRecord apiTestCaseRecord = apiTestCaseRunService.getApiTestCaseRecord(apiTestCase, apiReport.getId());
        // 创建报告和任务的关联关系
        ApiReportRelateTask apiReportRelateTask = apiCommonService.getApiReportRelateTask(request.getTaskItem().getId(), apiReport.getId());
        //初始化步骤
        ApiReportStep apiReportStep = getApiReportStep(testPlanApiCase, apiTestCase, apiReport.getId());
        apiReportService.insertApiReportDetail(apiReportStep, apiTestCaseRecord, apiReportRelateTask);

        return apiTestCaseRecord.getApiReportId();
    }

    public ApiReportStep getApiReportStep(TestPlanApiCase testPlanApiCase, ApiTestCase apiTestCase, String reportId) {
        ApiReportStep apiReportStep = apiTestCaseRunService.getApiReportStep(testPlanApiCase.getId(), apiTestCase.getName(), reportId, 1L);
        apiReportStep.setStepType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        return apiReportStep;
    }

    public TaskRequestDTO getTaskRequest(String reportId, String resourceId, String projectId, String runModule) {
        TaskRequestDTO taskRequest = apiTestCaseRunService.getTaskRequest(reportId, resourceId, projectId, runModule);
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        taskRequest.getTaskInfo().setNeedParseScript(true);
        return taskRequest;
    }

    public void checkReportIsTestPlan(String id) {
        ApiReportExample example = new ApiReportExample();
        example.createCriteria().andIdEqualTo(id).andTestPlanCaseIdNotEqualTo("NONE");
        if (apiReportMapper.countByExample(example) == 0) {
            throw new MSException("api_case_report_not_exist");
        }
    }

    /**
     * 批量移动
     *
     * @param request
     */
    public void batchMove(TestPlanApiCaseBatchMoveRequest request) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            moveCaseToCollection(ids, request.getTargetCollectionId());
        }
    }

    /**
     * 关联缺陷 (单条用例)
     *
     * @param request 请求参数
     * @param userId  用户ID
     */
    public void associateBug(TestPlanCaseAssociateBugRequest request, String userId) {
        super.associateBug(request, userId, CaseType.API_CASE.getKey());
    }

    private void moveCaseToCollection(List<String> ids, String targetCollectionId) {
        AtomicLong nextOrder = new AtomicLong(getNextOrder(targetCollectionId));
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanApiCaseMapper testPlanApiCaseMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
        ids.forEach(id -> {
            TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
            testPlanApiCase.setId(id);
            testPlanApiCase.setPos(nextOrder.getAndAdd(DEFAULT_NODE_INTERVAL_POS));
            testPlanApiCase.setTestPlanCollectionId(targetCollectionId);
            testPlanApiCaseMapper.updateByPrimaryKeySelective(testPlanApiCase);
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public Map<String, Long> getApiCaseModuleCount(ApiModuleRequest request, boolean deleted) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return Collections.emptyMap();
        }
        boolean isRepeat = true;
        if (StringUtils.isNotEmpty(request.getTestPlanId())) {
            isRepeat = this.checkTestPlanRepeatCase(request);
        }
        request.setModuleIds(null);
        //查找根据moduleIds查找模块下的接口数量 查非delete状态的
        List<ModuleCountDTO> moduleCountDTOList = extApiDefinitionModuleMapper.apiCaseCountModuleIdByRequest(request, deleted, isRepeat);
        long allCount = getAllCount(moduleCountDTOList);
        Map<String, Long> moduleCountMap = apiDefinitionModuleService.getModuleCountMap(request, moduleCountDTOList);
        moduleCountMap.put(DEBUG_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

    private boolean checkTestPlanRepeatCase(ApiModuleRequest request) {
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(request.getTestPlanId());
        return BooleanUtils.isTrue(testPlanConfig.getRepeatCase());
    }

    public long getAllCount(List<ModuleCountDTO> moduleCountDTOList) {
        long count = 0;
        for (ModuleCountDTO countDTO : moduleCountDTOList) {
            count += countDTO.getDataCount();
        }
        return count;
    }

    /**
     * 处理执行人为空过滤参数
     *
     * @param filter 请求参数
     */
    protected boolean filterCaseRequest(Map<String, List<String>> filter) {
        if (filter != null && filter.containsKey(EXECUTOR)) {
            List<String> filterExecutorIds = filter.get(EXECUTOR);
            if (CollectionUtils.isNotEmpty(filterExecutorIds)) {
                return filterExecutorIds.contains("-");
            }
        }
        return false;
    }

    public void batchAssociateBug(TestPlanApiCaseBatchAddBugRequest request, String bugId, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleAssociateBug(ids, userId, bugId, request.getTestPlanId());
        }

    }

    private void handleAssociateBug(List<String> ids, String userId, String bugId, String testPlanId) {
        SubListUtils.dealForSubList(ids, 500, (subList) -> {
            Map<String, String> caseMap = getCaseMap(subList);
            List<BugRelationCase> list = new ArrayList<>();
            subList.forEach(id -> {
                BugRelationCase bugRelationCase = new BugRelationCase();
                bugRelationCase.setId(IDGenerator.nextStr());
                bugRelationCase.setBugId(bugId);
                bugRelationCase.setCaseId(caseMap.get(id));
                bugRelationCase.setCaseType(CaseType.API_CASE.getKey());
                bugRelationCase.setCreateUser(userId);
                bugRelationCase.setCreateTime(System.currentTimeMillis());
                bugRelationCase.setUpdateTime(System.currentTimeMillis());
                bugRelationCase.setTestPlanCaseId(id);
                bugRelationCase.setTestPlanId(testPlanId);
                list.add(bugRelationCase);
            });
            bugRelationCaseMapper.batchInsert(list);
        });
    }

    public Map<String, String> getCaseMap(List<String> ids) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanApiCase> caseList = testPlanApiCaseMapper.selectByExample(example);
        return caseList.stream().collect(Collectors.toMap(TestPlanApiCase::getId, TestPlanApiCase::getApiCaseId));
    }

    public void batchAssociateBugByIds(TestPlanApiAssociateBugRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleAssociateBugByIds(ids, request, userId);
        }
    }

    public void handleAssociateBugByIds(List<String> ids, TestPlanApiAssociateBugRequest request, String userId) {
        SubListUtils.dealForSubList(ids, 500, (subList) -> {
            BugRelationCaseExample example = new BugRelationCaseExample();
            example.createCriteria().andTestPlanCaseIdIn(subList).andTestPlanIdEqualTo(request.getTestPlanId()).andBugIdIn(request.getBugIds());
            List<BugRelationCase> bugRelationCases = bugRelationCaseMapper.selectByExample(example);
            Map<String, List<String>> bugMap = bugRelationCases.stream()
                    .collect(Collectors.groupingBy(
                            BugRelationCase::getTestPlanCaseId,
                            Collectors.mapping(BugRelationCase::getBugId, Collectors.toList())
                    ));
            Map<String, String> caseMap = getCaseMap(subList);
            List<BugRelationCase> list = new ArrayList<>();
            subList.forEach(item -> {
                buildAssociateBugData(item, bugMap, list, request, caseMap, userId);
            });
            if (CollectionUtils.isNotEmpty(list)) {
                bugRelationCaseMapper.batchInsert(list);
            }
        });
    }

    private void buildAssociateBugData(String id, Map<String, List<String>> bugMap, List<BugRelationCase> list, TestPlanApiAssociateBugRequest request, Map<String, String> caseMap, String userId) {
        List<String> bugIds = new ArrayList<>(request.getBugIds());
        if (bugMap.containsKey(id)) {
            bugIds.removeAll(bugMap.get(id));
        }
        bugIds.forEach(bugId -> {
            BugRelationCase bugRelationCase = new BugRelationCase();
            bugRelationCase.setId(IDGenerator.nextStr());
            bugRelationCase.setBugId(bugId);
            bugRelationCase.setCaseId(caseMap.get(id));
            bugRelationCase.setCaseType(CaseType.API_CASE.getKey());
            bugRelationCase.setCreateUser(userId);
            bugRelationCase.setCreateTime(System.currentTimeMillis());
            bugRelationCase.setUpdateTime(System.currentTimeMillis());
            bugRelationCase.setTestPlanCaseId(id);
            bugRelationCase.setTestPlanId(request.getTestPlanId());
            list.add(bugRelationCase);
        });
    }

}
