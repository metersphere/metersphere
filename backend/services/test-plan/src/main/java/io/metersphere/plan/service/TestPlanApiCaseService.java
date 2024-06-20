package io.metersphere.plan.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCasePageRequest;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.mapper.ExtApiDefinitionModuleMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiExecuteService;
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
import io.metersphere.system.log.service.OperationLogService;
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
public class TestPlanApiCaseService extends TestPlanResourceService {

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
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
    private OperationLogService operationLogService;
    @Resource
    private ExtApiDefinitionModuleMapper extApiDefinitionModuleMapper;

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
    public List<TestPlanApiCasePageResponse> hasRelateApiCaseList(TestPlanApiCaseRequest request, boolean deleted) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new ArrayList<>();
        }
        List<TestPlanApiCasePageResponse> list = extTestPlanApiCaseMapper.relateApiCaseList(request, deleted);
        buildApiCaseResponse(list, request.getTestPlanId());
        return list;
    }

    private void buildApiCaseResponse(List<TestPlanApiCasePageResponse> apiCaseList, String testPlanId) {
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            Map<String, String> projectMap = getProject(apiCaseList);
            Map<String, String> userMap = getUserMap(apiCaseList);
            Map<String, String> moduleNameMap = getModuleName(apiCaseList);
            handleCaseAndEnv(apiCaseList, projectMap, userMap, testPlanId, moduleNameMap);
        }
    }

    private Map<String, String> getModuleName(List<TestPlanApiCasePageResponse> apiCaseList) {
        List<String> moduleIds = apiCaseList.stream().map(TestPlanApiCasePageResponse::getModuleId).toList();
        List<ApiDefinitionModule> modules = extApiDefinitionModuleMapper.getNameInfoByIds(moduleIds);
        return modules.stream()
                .collect(Collectors.toMap(ApiDefinitionModule::getId, ApiDefinitionModule::getName));
    }

    private void handleCaseAndEnv(List<TestPlanApiCasePageResponse> apiCaseList, Map<String, String> projectMap, Map<String, String> userMap, String testPlanId, Map<String, String> moduleNameMap) {
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
            List<String> ids = extTestPlanApiCaseMapper.getIds(request, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public List<TestPlanApiCase> getSelectIdAndCollectionId(TestPlanApiCaseBatchRequest request) {
        if (request.isSelectAll()) {
            List<TestPlanApiCase> testPlanApiCases = extTestPlanApiCaseMapper.getSelectIdAndCollectionId(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                testPlanApiCases.removeAll(request.getExcludeIds());
            }
            return testPlanApiCases;
        } else {
            TestPlanApiCaseExample example = new TestPlanApiCaseExample();
            example.createCriteria().andIdIn(request.getSelectIds());
            Map<String, TestPlanApiCase> testPlanApiCaseMap = testPlanApiCaseMapper.selectByExample(example)
                    .stream()
                    .collect(Collectors.toMap(TestPlanApiCase::getId, Function.identity()));
            List<TestPlanApiCase> testPlanApiCases = new ArrayList<>(request.getSelectIds().size());
            // 按ID的顺序排序
            for (String id : request.getSelectIds()) {
                TestPlanApiCase testPlanApiCase = testPlanApiCaseMap.get(id);
                if (testPlanApiCase != null) {
                    testPlanApiCases.add(testPlanApiCase);
                }
            }
            return testPlanApiCases;
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
        List<LogDTO> logDTOS = new ArrayList<>();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        //处理数据
        handleApiData(collectionAssociates.get(AssociateCaseType.API), user, testPlanApiCaseList, testPlan, logDTOS);
        handleApiCaseData(collectionAssociates.get(AssociateCaseType.API_CASE), user, testPlanApiCaseList, testPlan, logDTOS);
        if (CollectionUtils.isNotEmpty(testPlanApiCaseList)) {
            testPlanApiCaseMapper.batchInsert(testPlanApiCaseList);
            operationLogService.batchAdd(logDTOS);
        }
    }

    private void handleApiCaseData(List<BaseCollectionAssociateRequest> apiCaseList, SessionUser user, List<TestPlanApiCase> testPlanApiCaseList, TestPlan testPlan, List<LogDTO> logDTOS) {
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            List<String> ids = apiCaseList.stream().flatMap(item -> item.getIds().stream()).toList();
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria().andIdIn(ids);
            List<ApiTestCase> apiTestCaseList = apiTestCaseMapper.selectByExample(example);
            apiCaseList.forEach(apiCase -> {
                List<String> apiCaseIds = apiCase.getIds();
                if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                    List<ApiTestCase> apiTestCases = apiTestCaseList.stream().filter(item -> apiCaseIds.contains(item.getId())).collect(Collectors.toList());
                    buildTestPlanApiCase(testPlan, apiTestCases, apiCase.getCollectionId(), user, testPlanApiCaseList, logDTOS);
                }
            });
        }
    }

    private void handleApiData(List<BaseCollectionAssociateRequest> apiCaseList, SessionUser user, List<TestPlanApiCase> testPlanApiCaseList, TestPlan testPlan, List<LogDTO> logDTOS) {
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            List<String> ids = apiCaseList.stream().flatMap(item -> item.getIds().stream()).toList();
            boolean isRepeat = testPlanConfigService.isRepeatCase(testPlan.getId());
            List<ApiTestCase> apiTestCaseList = extTestPlanApiCaseMapper.selectApiCaseByDefinitionIds(ids, isRepeat, testPlan.getId());
            apiCaseList.forEach(apiCase -> {
                List<String> apiCaseIds = apiCase.getIds();
                if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                    List<ApiTestCase> apiTestCases = apiTestCaseList.stream().filter(item -> apiCaseIds.contains(item.getApiDefinitionId())).collect(Collectors.toList());
                    buildTestPlanApiCase(testPlan, apiTestCases, apiCase.getCollectionId(), user, testPlanApiCaseList, logDTOS);
                }
            });
        }
    }

    /**
     * 构建测试计划接口用例对象
     *
     * @param testPlan
     * @param apiTestCases
     * @param collectionId
     * @param user
     * @param testPlanApiCaseList
     */
    private void buildTestPlanApiCase(TestPlan testPlan, List<ApiTestCase> apiTestCases, String collectionId, SessionUser user, List<TestPlanApiCase> testPlanApiCaseList, List<LogDTO> logDTOS) {
        super.checkCollection(testPlan.getId(), collectionId, CaseType.API_CASE.getKey());
        AtomicLong nextOrder = new AtomicLong(getNextOrder(collectionId));
        apiTestCases.forEach(apiTestCase -> {
            TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
            testPlanApiCase.setId(IDGenerator.nextStr());
            testPlanApiCase.setTestPlanCollectionId(collectionId);
            testPlanApiCase.setTestPlanId(testPlan.getId());
            testPlanApiCase.setApiCaseId(apiTestCase.getId());
            testPlanApiCase.setEnvironmentId(apiTestCase.getEnvironmentId());
            testPlanApiCase.setCreateTime(System.currentTimeMillis());
            testPlanApiCase.setCreateUser(user.getId());
            testPlanApiCase.setPos(nextOrder.getAndAdd(DEFAULT_NODE_INTERVAL_POS));
            testPlanApiCase.setExecuteUser(apiTestCase.getCreateUser());
            testPlanApiCase.setLastExecResult(ExecStatus.PENDING.name());
            testPlanApiCaseList.add(testPlanApiCase);
            buildLog(logDTOS, testPlan, user, apiTestCase);
        });
    }

    private void buildLog(List<LogDTO> logDTOS, TestPlan testPlan, SessionUser user, ApiTestCase apiTestCase) {
        LogDTO dto = new LogDTO(
                testPlan.getProjectId(),
                user.getLastOrganizationId(),
                testPlan.getId(),
                user.getId(),
                OperationLogType.ASSOCIATE.name(),
                OperationLogModule.TEST_PLAN,
                Translator.get("log.test_plan.api_case") + ":" + apiTestCase.getName());
        dto.setHistory(true);
        dto.setPath("/test-plan/mind/data/edit");
        dto.setMethod(HttpMethodConstants.POST.name());
        logDTOS.add(dto);
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
        testPlanService.setTestPlanUnderway(testPlanApiCase.getTestPlanId());
        testPlanService.setActualStartTime(testPlanApiCase.getTestPlanId());
        ApiTestCase apiTestCase = apiTestCaseService.checkResourceExist(testPlanApiCase.getApiCaseId());
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(testPlanApiCase.getTestPlanCollectionId());
        runModeConfig.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanApiCase.getEnvironmentId()));
        runModeConfig.setRunMode(ApiBatchRunMode.PARALLEL.name());
        TaskRequestDTO taskRequest = getTaskRequest(reportId, id, apiTestCase.getProjectId(), ApiExecuteRunMode.RUN.name());
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

        // 初始化报告
        initApiReport(apiTestCase, testPlanApiCase, reportId, runModeConfig, userId);

        return apiExecuteService.execute(taskRequest);
    }

    public TestPlanApiCase checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(testPlanApiCaseMapper.selectByPrimaryKey(id), "api_test_case_not_exist");
    }

    /**
     * 获取执行脚本
     */
    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(taskItem.getResourceId());
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(testPlanApiCase.getApiCaseId());
        apiTestCase.setEnvironmentId(testPlanApiCase.getEnvironmentId());
        return apiTestCaseService.getRunScript(request, apiTestCase);
    }

    /**
     * 预生成用例的执行报告
     *
     * @return
     */
    public ApiTestCaseRecord initApiReport(ApiTestCase apiTestCase, TestPlanApiCase testPlanApiCase, String reportId, ApiRunModeConfigDTO runModeConfig, String userId) {
        // 初始化报告
        ApiReport apiReport = apiTestCaseService.getApiReport(apiTestCase, reportId, runModeConfig.getPoolId(), userId);
        apiReport.setEnvironmentId(runModeConfig.getEnvironmentId());
        apiReport.setTestPlanCaseId(testPlanApiCase.getId());

        // 创建报告和用例的关联关系
        ApiTestCaseRecord apiTestCaseRecord = apiTestCaseService.getApiTestCaseRecord(apiTestCase, apiReport);

        apiReportService.insertApiReport(List.of(apiReport), List.of(apiTestCaseRecord));

        //初始化步骤
        apiReportService.insertApiReportStep(List.of(getApiReportStep(testPlanApiCase, apiTestCase, reportId)));
        return apiTestCaseRecord;
    }

    public ApiReportStep getApiReportStep(TestPlanApiCase testPlanApiCase, ApiTestCase apiTestCase, String reportId) {
        ApiReportStep apiReportStep = apiTestCaseService.getApiReportStep(testPlanApiCase.getId(), apiTestCase.getName(), reportId, 1L);
        apiReportStep.setStepType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        return apiReportStep;
    }

    public TaskRequestDTO getTaskRequest(String reportId, String resourceId, String projectId, String runModule) {
        TaskRequestDTO taskRequest = apiTestCaseService.getTaskRequest(reportId, resourceId, projectId, runModule);
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
}
