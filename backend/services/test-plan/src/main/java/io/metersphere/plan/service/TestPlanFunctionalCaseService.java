package io.metersphere.plan.service;

import com.alibaba.excel.util.BooleanUtils;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.domain.BugRelationCaseExample;
import io.metersphere.bug.dto.CaseRelateBugDTO;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
import io.metersphere.bug.mapper.ExtBugRelateCaseMapper;
import io.metersphere.bug.service.BugStatusService;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.functional.constants.CaseFileSourceType;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseModule;
import io.metersphere.functional.domain.FunctionalCaseTest;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseModuleMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseTestMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.service.FunctionalCaseAttachmentService;
import io.metersphere.functional.service.FunctionalCaseModuleService;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.constants.TreeTypeEnums;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.*;
import io.metersphere.plan.mapper.*;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.provider.BaseAssociateBugProvider;
import io.metersphere.request.AssociateBugPageRequest;
import io.metersphere.request.BugPageProviderRequest;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.AssociateCaseDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.log.aspect.OperationLogAspect;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.mapper.ExtUserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
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

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanFunctionalCaseService extends TestPlanResourceService {
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private ExtTestPlanFunctionalCaseMapper extTestPlanFunctionalCaseMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanResourceLogService testPlanResourceLogService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private FunctionalCaseService functionalCaseService;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private ExtBugRelateCaseMapper bugRelateCaseMapper;
    @Resource
    private BugRelationCaseMapper bugRelationCaseMapper;
    @Resource
    private ExtTestPlanModuleMapper extTestPlanModuleMapper;
    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;
    @Resource
    private BaseAssociateBugProvider baseAssociateBugProvider;
    @Resource
    private BugMapper bugMapper;
    @Resource
    private TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;
    @Resource
    private ExtTestPlanCaseExecuteHistoryMapper extTestPlanCaseExecuteHistoryMapper;
    @Resource
    private FunctionalCaseAttachmentService functionalCaseAttachmentService;
    @Resource
    private TestPlanSendNoticeService testPlanSendNoticeService;
    @Resource
    private BugStatusService bugStatusService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;

    @Resource
    private ExtUserMapper extUserMapper;
    private static final String CASE_MODULE_COUNT_ALL = "all";
    @Resource
    private ExtFunctionalCaseModuleMapper extFunctionalCaseModuleMapper;
    @Resource
    private ExtTestPlanCollectionMapper extTestPlanCollectionMapper;
    @Resource
    private TestPlanConfigService testPlanConfigService;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private ExtFunctionalCaseTestMapper extFunctionalCaseTestMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private TestPlanApiScenarioService testPlanApiScenarioService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;

    @Override
    public List<TestPlanResourceExecResultDTO> selectDistinctExecResult(String projectId) {
        return extTestPlanFunctionalCaseMapper.selectDistinctExecResult(projectId);
    }
    @Override
    public long copyResource(String originalTestPlanId, String newTestPlanId, Map<String, String> oldCollectionIdToNewCollectionId, String operator, long operatorTime) {
        List<TestPlanFunctionalCase> copyList = new ArrayList<>();
        String defaultCollectionId = extTestPlanCollectionMapper.selectDefaultCollectionId(newTestPlanId, CaseType.SCENARIO_CASE.getKey());
        extTestPlanFunctionalCaseMapper.selectByTestPlanIdAndNotDeleted(originalTestPlanId).forEach(originalCase -> {
            TestPlanFunctionalCase newCase = new TestPlanFunctionalCase();
            BeanUtils.copyBean(newCase, originalCase);
            newCase.setId(IDGenerator.nextStr());
            newCase.setTestPlanId(newTestPlanId);
            newCase.setCreateTime(operatorTime);
            newCase.setCreateUser(operator);
            newCase.setLastExecTime(0L);
            newCase.setTestPlanCollectionId(oldCollectionIdToNewCollectionId.get(newCase.getTestPlanCollectionId()) == null ? defaultCollectionId : oldCollectionIdToNewCollectionId.get(newCase.getTestPlanCollectionId()));
            newCase.setLastExecResult(ExecStatus.PENDING.name());
            copyList.add(newCase);
        });

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanFunctionalCaseMapper batchInsertMapper = sqlSession.getMapper(TestPlanFunctionalCaseMapper.class);
        copyList.forEach(item -> batchInsertMapper.insert(item));
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        return copyList.size();
    }

    @Override
    public void deleteBatchByTestPlanId(List<String> testPlanIdList) {
        if (CollectionUtils.isNotEmpty(testPlanIdList)) {
            TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
            testPlanFunctionalCaseExample.createCriteria().andTestPlanIdIn(testPlanIdList);
            testPlanFunctionalCaseMapper.deleteByExample(testPlanFunctionalCaseExample);

            TestPlanCaseExecuteHistoryExample testPlanCaseExecuteHistoryExample = new TestPlanCaseExecuteHistoryExample();
            testPlanCaseExecuteHistoryExample.createCriteria().andTestPlanIdIn(testPlanIdList);
            testPlanCaseExecuteHistoryMapper.deleteByExample(testPlanCaseExecuteHistoryExample);
        }
    }


    @Override
    public long getNextOrder(String collectionId) {
        Long maxPos = extTestPlanFunctionalCaseMapper.getMaxPosByCollectionId(collectionId);
        if (maxPos == null) {
            return DEFAULT_NODE_INTERVAL_POS;
        } else {
            return maxPos + DEFAULT_NODE_INTERVAL_POS;
        }
    }

    @Override
    public void updatePos(String id, long pos) {
        extTestPlanFunctionalCaseMapper.updatePos(id, pos);
    }

    @Override
    public Map<String, Long> caseExecResultCount(String testPlanId) {
        List<TestPlanCaseRunResultCount> runResultCounts = extTestPlanFunctionalCaseMapper.selectCaseExecResultCount(testPlanId);
        return runResultCounts.stream().collect(Collectors.toMap(TestPlanCaseRunResultCount::getResult, TestPlanCaseRunResultCount::getResultCount));
    }

    @Override
    public void refreshPos(String testPlanId) {
        List<String> functionalCaseIdList = extTestPlanFunctionalCaseMapper.selectIdByTestPlanIdOrderByPos(testPlanId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtTestPlanFunctionalCaseMapper batchUpdateMapper = sqlSession.getMapper(ExtTestPlanFunctionalCaseMapper.class);
        for (int i = 0; i < functionalCaseIdList.size(); i++) {
            batchUpdateMapper.updatePos(functionalCaseIdList.get(i), i * DEFAULT_NODE_INTERVAL_POS);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }


    public void deleteTestPlanResource(@Validated TestPlanResourceAssociationParam associationParam) {
        TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
        testPlanFunctionalCaseExample.createCriteria().andIdIn(associationParam.getResourceIdList());
        testPlanFunctionalCaseMapper.deleteByExample(testPlanFunctionalCaseExample);
        // 取消关联用例需同步删除计划-用例缺陷关系表
        BugRelationCaseExample example = new BugRelationCaseExample();
        example.createCriteria().andTestPlanCaseIdIn(associationParam.getResourceIdList());
        bugRelationCaseMapper.deleteByExample(example);
        extTestPlanCaseExecuteHistoryMapper.updateDeleted(associationParam.getResourceIdList(), true);
    }

    public TestPlanOperationResponse sortNode(ResourceSortRequest request, LogInsertModule logInsertModule) {
        TestPlanFunctionalCase dragNode = testPlanFunctionalCaseMapper.selectByPrimaryKey(request.getMoveId());
        if (dragNode == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        TestPlanOperationResponse response = new TestPlanOperationResponse();
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                request.getTestCollectionId(),
                super.getNodeMoveRequest(request, false),
                extTestPlanFunctionalCaseMapper::selectDragInfoById,
                extTestPlanFunctionalCaseMapper::selectNodeByPosOperator
        );
        super.sort(sortDTO);
        response.setOperationCount(1);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(dragNode.getTestPlanId());
        testPlanResourceLogService.saveSortLog(testPlan, request.getMoveId(), new ResourceLogInsertModule(TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE, logInsertModule));
        return response;
    }


    public List<TestPlanCasePageResponse> getFunctionalCasePage(TestPlanCaseRequest request, boolean deleted) {
        List<TestPlanCasePageResponse> functionalCaseLists = extTestPlanFunctionalCaseMapper.getCasePage(request, deleted, request.getSortString());
        if (CollectionUtils.isEmpty(functionalCaseLists)) {
            return new ArrayList<>();
        }
        //处理自定义字段值
        return handleCustomFields(functionalCaseLists, request.getProjectId());
    }

    private List<TestPlanCasePageResponse> handleCustomFields(List<TestPlanCasePageResponse> functionalCaseLists, String projectId) {
        List<String> ids = functionalCaseLists.stream().map(TestPlanCasePageResponse::getCaseId).collect(Collectors.toList());
        Map<String, List<FunctionalCaseCustomFieldDTO>> collect = functionalCaseService.getCaseCustomFiledMap(ids, projectId);
        Set<String> userIds = extractUserIds(functionalCaseLists);
        List<String> relateIds = functionalCaseLists.stream().map(TestPlanCasePageResponse::getId).collect(Collectors.toList());
        Map<String, List<CaseRelateBugDTO>> bugListMap = getBugData(relateIds, functionalCaseLists.getFirst().getTestPlanId());
        List<SelectOption> statusOption = bugStatusService.getHeaderStatusOption(projectId);
        Map<String, String> statusMap = statusOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));
        List<String> moduleIds = functionalCaseLists.stream().map(TestPlanCasePageResponse::getModuleId).toList();
        List<FunctionalCaseModule> modules = extFunctionalCaseModuleMapper.getNameInfoByIds(moduleIds);
        Map<String, String> moduleNameMap = modules.stream().collect(Collectors.toMap(FunctionalCaseModule::getId, FunctionalCaseModule::getName));
        functionalCaseLists.forEach(testPlanCasePageResponse -> {
            testPlanCasePageResponse.setCustomFields(collect.get(testPlanCasePageResponse.getCaseId()));
            testPlanCasePageResponse.setCreateUserName(userMap.get(testPlanCasePageResponse.getCreateUser()));
            testPlanCasePageResponse.setExecuteUserName(userMap.get(testPlanCasePageResponse.getExecuteUser()));
            testPlanCasePageResponse.setModuleName(StringUtils.isNotBlank(moduleNameMap.get(testPlanCasePageResponse.getModuleId())) ? moduleNameMap.get(testPlanCasePageResponse.getModuleId()) : Translator.get("functional_case.module.default.name"));
            if (bugListMap.containsKey(testPlanCasePageResponse.getCaseId())) {
                List<CaseRelateBugDTO> bugDTOList = bugListMap.get(testPlanCasePageResponse.getCaseId());
                testPlanCasePageResponse.setBugList(handleStatus(bugDTOList, statusMap));
                testPlanCasePageResponse.setBugCount(bugDTOList.size());
            }
        });
        return functionalCaseLists;

    }

    private List<CaseRelateBugDTO> handleStatus(List<CaseRelateBugDTO> bugDTOList, Map<String, String> statusMap) {
        bugDTOList.forEach(bugDTO -> {
            bugDTO.setStatus(statusMap.get(bugDTO.getStatus()));
        });
        return bugDTOList;
    }

    private Map<String, List<CaseRelateBugDTO>> getBugData(List<String> ids, String testPlanId) {
        List<CaseRelateBugDTO> bugList = bugRelateCaseMapper.getBugCountByIds(ids, testPlanId);
        return bugList.stream().collect(Collectors.groupingBy(CaseRelateBugDTO::getCaseId));
    }


    public Set<String> extractUserIds(List<TestPlanCasePageResponse> list) {
        return list.stream()
                .flatMap(testPlanCasePageResponse -> Stream.of(testPlanCasePageResponse.getUpdateUser(), testPlanCasePageResponse.getCreateUser(), testPlanCasePageResponse.getExecuteUser()))
                .collect(Collectors.toSet());
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
     * 已关联功能用例规划视图树
     *
     * @param testPlanId
     * @return
     */
    private List<BaseTreeNode> getCollectionTree(String testPlanId) {
        List<BaseTreeNode> returnList = new ArrayList<>();
        TestPlanCollectionExample collectionExample = new TestPlanCollectionExample();
        collectionExample.createCriteria().andTypeEqualTo(CaseType.FUNCTIONAL_CASE.getKey()).andParentIdNotEqualTo(ModuleConstants.ROOT_NODE_PARENT_ID).andTestPlanIdEqualTo(testPlanId);
        List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(collectionExample);
        testPlanCollections.forEach(item -> {
            BaseTreeNode baseTreeNode = new BaseTreeNode(item.getId(), Translator.get(item.getName(), item.getName()), CaseType.FUNCTIONAL_CASE.getKey());
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
        List<ProjectOptionDTO> moduleLists = extTestPlanFunctionalCaseMapper.selectRootIdByTestPlanId(testPlanId);
        // 获取所有的项目id
        List<String> projectIds = moduleLists.stream().map(ProjectOptionDTO::getName).distinct().toList();
        // moduleLists中id=root的数据
        List<ProjectOptionDTO> rootModuleList = moduleLists.stream().filter(item -> StringUtils.equals(item.getId(), ModuleConstants.DEFAULT_NODE_ID)).toList();

        Map<String, List<ProjectOptionDTO>> projectRootMap = rootModuleList.stream().collect(Collectors.groupingBy(ProjectOptionDTO::getName));
        List<FunctionalCaseModuleDTO> functionalModuleIds = extTestPlanFunctionalCaseMapper.selectBaseByProjectIdAndTestPlanId(testPlanId);
        Map<String, List<FunctionalCaseModuleDTO>> projectModuleMap = functionalModuleIds.stream().collect(Collectors.groupingBy(FunctionalCaseModule::getProjectId));

        projectIds.forEach(projectId -> {
            // 如果projectRootMap中没有projectId，说明该项目没有根节点 不需要创
            // projectModuleMap中没有projectId，说明该项目没有模块 不需要创建
            // 如果都有 需要创建完整的数结构
            boolean needCreatRoot = MapUtils.isNotEmpty(projectRootMap) && projectRootMap.containsKey(projectId);
            boolean needCreatModule = MapUtils.isNotEmpty(projectModuleMap) && projectModuleMap.containsKey(projectId);
            // 项目名称是
            String projectName = needCreatModule ? projectModuleMap.get(projectId).getFirst().getProjectName() : projectRootMap.get(projectId).getFirst().getProjectName();
            // 构建项目那一层级
            BaseTreeNode projectNode = new BaseTreeNode(projectId, projectName, "PROJECT");
            returnList.add(projectNode);
            List<BaseTreeNode> nodeByNodeIds = new ArrayList<>();
            if (needCreatModule) {
                List<String> projectModuleIds = projectModuleMap.get(projectId).stream().map(FunctionalCaseModuleDTO::getId).toList();
                nodeByNodeIds = functionalCaseModuleService.getNodeByNodeIds(projectModuleIds);
            }
            List<BaseTreeNode> baseTreeNodes = functionalCaseModuleService.buildTreeAndCountResource(nodeByNodeIds, needCreatRoot, Translator.get("functional_case.module.default.name"));
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


    public Map<String, Long> moduleCount(TestPlanCaseModuleRequest request) {
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
    private Map<String, Long> getCollectionCount(TestPlanCaseModuleRequest request) {
        request.setCollectionId(null);
        Map<String, Long> projectModuleCountMap = new HashMap<>();
        List<ModuleCountDTO> list = extTestPlanFunctionalCaseMapper.collectionCountByRequest(request);
        list.forEach(item -> {
            projectModuleCountMap.put(item.getModuleId(), (long) item.getDataCount());
        });
        long allCount = extTestPlanFunctionalCaseMapper.caseCount(request, false);
        projectModuleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return projectModuleCountMap;
    }

    /**
     * 已关联接口用例模块树统计
     *
     * @param request
     * @return
     */
    private Map<String, Long> getModuleCount(TestPlanCaseModuleRequest request) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<FunctionalCaseModuleCountDTO> projectModuleCountDTOList = extTestPlanFunctionalCaseMapper.countModuleIdByRequest(request, false);
        Map<String, List<FunctionalCaseModuleCountDTO>> projectCountMap = projectModuleCountDTOList.stream().collect(Collectors.groupingBy(FunctionalCaseModuleCountDTO::getProjectId));
        Map<String, Long> projectModuleCountMap = projectModuleCountDTOList.stream().collect(Collectors.groupingBy(FunctionalCaseModuleCountDTO::getModuleId, Collectors.summingLong(FunctionalCaseModuleCountDTO::getDataCount)));
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
        long allCount = extTestPlanFunctionalCaseMapper.caseCount(request, false);
        projectModuleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return projectModuleCountMap;
    }


    public Map<String, Long> getModuleCountMap(String projectId, String testPlanId, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(projectId, testPlanId, moduleCountDTOList);

        //通过广度遍历的方式构建返回值
        return functionalCaseModuleService.getIdCountMapByBreadth(treeNodeList);
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, String testPlanId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<String> moduleIds = extTestPlanModuleMapper.selectIdByProjectIdAndTestPlanId(projectId, testPlanId);
        List<BaseTreeNode> nodeByNodeIds = functionalCaseModuleService.getNodeByNodeIds(moduleIds);
        return functionalCaseModuleService.buildTreeAndCountResource(nodeByNodeIds, moduleCountDTOList, true, Translator.get("functional_case.module.default.name"));


    }

    public TestPlanAssociationResponse disassociate(BasePlanCaseBatchRequest request, LogInsertModule logInsertModule) {
        List<String> selectIds = doSelectIds(request);
        return super.disassociate(
                TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE,
                request,
                logInsertModule,
                selectIds,
                this::deleteTestPlanResource);
    }

    public List<String> doSelectIds(BasePlanCaseBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extTestPlanFunctionalCaseMapper.getIds(request, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public List<BugProviderDTO> bugPage(BugPageProviderRequest request) {
        return baseAssociateBugProvider.getBugList("bug_relation_case", "test_plan_case_id", "bug_id", request);
    }

    public void associateBug(TestPlanCaseAssociateBugRequest request, String userId) {
        List<String> ids = baseAssociateBugProvider.getSelectBugs(request, false);
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 100, subList -> {
                List<BugRelationCase> list = new ArrayList<>();
                subList.forEach(id -> {
                    BugRelationCase bugRelationCase = new BugRelationCase();
                    bugRelationCase.setId(IDGenerator.nextStr());
                    bugRelationCase.setBugId(id);
                    bugRelationCase.setCaseId(request.getCaseId());
                    bugRelationCase.setCaseType(CaseType.FUNCTIONAL_CASE.getKey());
                    bugRelationCase.setCreateUser(userId);
                    bugRelationCase.setCreateTime(System.currentTimeMillis());
                    bugRelationCase.setUpdateTime(System.currentTimeMillis());
                    bugRelationCase.setTestPlanCaseId(request.getTestPlanCaseId());
                    bugRelationCase.setTestPlanId(request.getTestPlanId());
                    list.add(bugRelationCase);
                });
                bugRelationCaseMapper.batchInsert(list);
            });

        }
    }

    public void disassociateBug(String id) {
        baseAssociateBugProvider.disassociateBug(id);
    }

    public LogDTO disassociateBugLog(String id) {
        BugRelationCase bugRelationCase = bugRelationCaseMapper.selectByPrimaryKey(id);
        if (bugRelationCase != null) {
            Bug bug = bugMapper.selectByPrimaryKey(bugRelationCase.getBugId());
            LogDTO dto = new LogDTO(
                    null,
                    null,
                    bugRelationCase.getBugId(),
                    null,
                    OperationLogType.DISASSOCIATE.name(),
                    OperationLogModule.TEST_PLAN,
                    bug.getTitle() + "缺陷");
            dto.setPath(OperationLogAspect.getPath());
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(bugRelationCase));
            return dto;
        }
        return null;
    }

    /**
     * 执行功能用例
     *
     * @param request
     * @param logInsertModule
     */
    public void run(TestPlanCaseRunRequest request, LogInsertModule logInsertModule) {
        TestPlanFunctionalCase functionalCase = new TestPlanFunctionalCase();
        functionalCase.setLastExecResult(request.getLastExecResult());
        functionalCase.setLastExecTime(System.currentTimeMillis());
        functionalCase.setExecuteUser(logInsertModule.getOperator());
        functionalCase.setId(request.getId());
        testPlanFunctionalCaseMapper.updateByPrimaryKeySelective(functionalCase);

        //更新用例表执行状态
        updateFunctionalCaseStatus(Collections.singletonList(request.getCaseId()), request.getLastExecResult());

        //执行记录
        TestPlanCaseExecuteHistory executeHistory = buildHistory(request, logInsertModule.getOperator());
        handleFileAndNotice(request.getCaseId(), request.getProjectId(), request.getPlanCommentFileIds(), logInsertModule.getOperator(), CaseFileSourceType.PLAN_COMMENT.toString(), request.getNotifier(), request.getTestPlanId(), request.getLastExecResult());
        testPlanCaseExecuteHistoryMapper.insert(executeHistory);

    }

    /**
     * 更新功能用例表的执行状态
     *
     * @param ids
     * @param lastExecResult
     */
    private void updateFunctionalCaseStatus(List<String> ids, String lastExecResult) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper functionalCaseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        ids.forEach(id -> {
            FunctionalCase functionalCase = new FunctionalCase();
            functionalCase.setId(id);
            functionalCase.setLastExecuteResult(lastExecResult);
            functionalCaseMapper.updateByPrimaryKeySelective(functionalCase);
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }


    private TestPlanCaseExecuteHistory buildHistory(TestPlanCaseRunRequest request, String operator) {
        TestPlanCaseExecuteHistory executeHistory = new TestPlanCaseExecuteHistory();
        executeHistory.setId(IDGenerator.nextStr());
        executeHistory.setTestPlanCaseId(request.getId());
        executeHistory.setTestPlanId(request.getTestPlanId());
        executeHistory.setCaseId(request.getCaseId());
        executeHistory.setStatus(request.getLastExecResult());
        if (StringUtils.isNotBlank(request.getContent())) {
            executeHistory.setContent(request.getContent().getBytes());
        }
        executeHistory.setSteps(StringUtils.defaultIfBlank(request.getStepsExecResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        executeHistory.setDeleted(false);
        executeHistory.setNotifier(request.getNotifier());
        executeHistory.setCreateUser(operator);
        executeHistory.setCreateTime(System.currentTimeMillis());
        return executeHistory;
    }

    public List<BugProviderDTO> hasAssociateBugPage(AssociateBugPageRequest request) {
        return baseAssociateBugProvider.hasTestPlanAssociateBugPage(request);
    }


    /**
     * 批量执行功能用例
     *
     * @param request
     * @param logInsertModule
     */
    public void batchRun(TestPlanCaseBatchRunRequest request, LogInsertModule logInsertModule) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleBatchRun(ids, request, logInsertModule);
        }

    }

    private void handleBatchRun(List<String> ids, TestPlanCaseBatchRunRequest request, LogInsertModule logInsertModule) {
        //更新状态
        extTestPlanFunctionalCaseMapper.batchUpdate(ids, request.getLastExecResult(), System.currentTimeMillis(), logInsertModule.getOperator());

        //执行记录
        TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanFunctionalCase> functionalCases = testPlanFunctionalCaseMapper.selectByExample(example);
        List<String> caseIds = functionalCases.stream().map(TestPlanFunctionalCase::getFunctionalCaseId).collect(Collectors.toList());
        Map<String, String> idsMap = functionalCases.stream().collect(Collectors.toMap(TestPlanFunctionalCase::getId, TestPlanFunctionalCase::getFunctionalCaseId));
        List<TestPlanCaseExecuteHistory> historyList = getExecHistory(ids, request, logInsertModule, idsMap);
        testPlanCaseExecuteHistoryMapper.batchInsert(historyList);

        updateFunctionalCaseStatus(caseIds, request.getLastExecResult());

    }

    private List<TestPlanCaseExecuteHistory> getExecHistory(List<String> ids, TestPlanCaseBatchRunRequest request, LogInsertModule logInsertModule, Map<String, String> idsMap) {

        List<TestPlanCaseExecuteHistory> historyList = new ArrayList<>();
        ids.forEach(id -> {
            TestPlanCaseExecuteHistory executeHistory = new TestPlanCaseExecuteHistory();
            executeHistory.setId(IDGenerator.nextStr());
            executeHistory.setTestPlanCaseId(id);
            executeHistory.setTestPlanId(request.getTestPlanId());
            executeHistory.setCaseId(idsMap.get(id));
            executeHistory.setStatus(request.getLastExecResult());
            executeHistory.setContent(request.getContent().getBytes());
            executeHistory.setDeleted(false);
            executeHistory.setNotifier(request.getNotifier());
            executeHistory.setCreateUser(logInsertModule.getOperator());
            executeHistory.setCreateTime(System.currentTimeMillis());
            historyList.add(executeHistory);

            handleFileAndNotice(idsMap.get(id), request.getProjectId(), request.getPlanCommentFileIds(), logInsertModule.getOperator(), CaseFileSourceType.PLAN_COMMENT.toString(), request.getNotifier(), request.getTestPlanId(), request.getLastExecResult());


        });
        return historyList;
    }

    private void handleFileAndNotice(String caseId, String projectId, List<String> uploadFileIds, String userId, String fileSource, String notifier, String testPlanId, String lastExecResult) {
        //富文本评论的处理
        functionalCaseAttachmentService.uploadMinioFile(caseId, projectId, uploadFileIds, userId, fileSource);

        //发通知
        if (StringUtils.isNotBlank(notifier)) {
            List<String> relatedUsers = Arrays.asList(notifier.split(";"));
            testPlanSendNoticeService.sendNoticeCase(relatedUsers, userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.EXECUTE_AT, testPlanId);
        }

        if (StringUtils.equalsIgnoreCase(lastExecResult, ResultStatus.SUCCESS.name())) {
            //成功 发送通知
            testPlanSendNoticeService.sendNoticeCase(new ArrayList<>(), userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.EXECUTE_PASSED, testPlanId);
        }

        if (StringUtils.equalsIgnoreCase(lastExecResult, ResultStatus.ERROR.name())) {
            //失败 发送通知
            testPlanSendNoticeService.sendNoticeCase(new ArrayList<>(), userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.EXECUTE_FAIL, testPlanId);
        }
    }


    /**
     * 批量更新执行人
     *
     * @param request
     */
    public void batchUpdateExecutor(TestPlanCaseUpdateRequest request) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            extTestPlanFunctionalCaseMapper.batchUpdateExecutor(ids, request.getUserId());
        }
    }

    public List<TestPlanCaseExecHistoryResponse> getCaseExecHistory(TestPlanCaseExecHistoryRequest request) {
        List<TestPlanCaseExecHistoryResponse> list = extTestPlanCaseExecuteHistoryMapper.getCaseExecHistory(request);
        list.forEach(item -> {
            if (item.getContent() != null) {
                item.setContentText(new String(item.getContent(), StandardCharsets.UTF_8));
            }
            if (item.getSteps() != null) {
                item.setStepsExecResult(new String(item.getSteps(), StandardCharsets.UTF_8));
            }
        });
        return list;
    }

    public TestPlanCaseDetailResponse getFunctionalCaseDetail(String id, String userId) {
        TestPlanFunctionalCase planFunctionalCase = testPlanFunctionalCaseMapper.selectByPrimaryKey(id);
        String caseId = planFunctionalCase.getFunctionalCaseId();
        FunctionalCaseDetailDTO functionalCaseDetail = functionalCaseService.getFunctionalCaseDetail(caseId, userId, false);
        String caseDetailSteps = functionalCaseDetail.getSteps();
        List<TestPlanCaseExecuteHistory> testPlanCaseExecuteHistories = extTestPlanCaseExecuteHistoryMapper.selectSteps(id, caseId);
        List<FunctionalCaseStepDTO> functionalCaseStepDTOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(testPlanCaseExecuteHistories)) {
            TestPlanCaseExecuteHistory testPlanCaseExecuteHistory = testPlanCaseExecuteHistories.getFirst();
            if (StringUtils.isNotBlank(caseDetailSteps)) {
                List<FunctionalCaseStepDTO> newCaseSteps = JSON.parseArray(caseDetailSteps, FunctionalCaseStepDTO.class);
                compareStep(testPlanCaseExecuteHistory, newCaseSteps);
                functionalCaseStepDTOS = newCaseSteps;
                functionalCaseDetail.setSteps(JSON.toJSONString(functionalCaseStepDTOS));
            }
        } else {
            if (StringUtils.isNotBlank(caseDetailSteps)) {
                functionalCaseStepDTOS = JSON.parseArray(caseDetailSteps, FunctionalCaseStepDTO.class);
            }
            functionalCaseDetail.setSteps(JSON.toJSONString(functionalCaseStepDTOS));
        }
        TestPlanCaseDetailResponse response = new TestPlanCaseDetailResponse();
        BeanUtils.copyBean(response, functionalCaseDetail);
        response.setLastExecuteResult(planFunctionalCase.getLastExecResult());

        TestPlanCaseExecuteHistoryExample testPlanCaseExecuteHistoryExample = new TestPlanCaseExecuteHistoryExample();
        testPlanCaseExecuteHistoryExample.createCriteria().andCaseIdEqualTo(caseId).andTestPlanCaseIdEqualTo(id).andDeletedEqualTo(false);
        testPlanCaseExecuteHistoryExample.setOrderByClause("create_time DESC");
        response.setRunListCount((int) testPlanCaseExecuteHistoryMapper.countByExample(testPlanCaseExecuteHistoryExample));
        response.setBugListCount(getBugListCount(id, planFunctionalCase.getTestPlanId()));
        return response;
    }

    private Integer getBugListCount(String id, String testPlanId) {
        BugRelationCaseExample example = new BugRelationCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId).andTestPlanCaseIdEqualTo(id);
        return (int) bugRelationCaseMapper.countByExample(example);
    }

    private static void compareStep(TestPlanCaseExecuteHistory testPlanCaseExecuteHistory, List<FunctionalCaseStepDTO> newCaseSteps) {
        if (testPlanCaseExecuteHistory.getSteps() != null) {
            String historyStepStr = new String(testPlanCaseExecuteHistory.getSteps(), StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(historyStepStr)) {
                List<FunctionalCaseStepDTO> historySteps = JSON.parseArray(historyStepStr, FunctionalCaseStepDTO.class);
                Map<String, FunctionalCaseStepDTO> historyStepMap = historySteps.stream().collect(Collectors.toMap(FunctionalCaseStepDTO::getId, t -> t));
                newCaseSteps.forEach(newCaseStep -> {
                    setHistoryInfo(newCaseStep, historyStepMap);
                });
            }
        }
    }

    private static void setHistoryInfo(FunctionalCaseStepDTO newCaseStep, Map<String, FunctionalCaseStepDTO> historyStepMap) {
        FunctionalCaseStepDTO historyStep = historyStepMap.get(newCaseStep.getId());
        if (historyStep != null && StringUtils.equals(historyStep.getDesc(), newCaseStep.getDesc()) && StringUtils.equals(historyStep.getResult(), newCaseStep.getResult())) {
            newCaseStep.setExecuteResult(historyStep.getExecuteResult());
            newCaseStep.setActualResult(historyStep.getActualResult());
        }
    }

    /**
     * 获取项目下的所有用户
     *
     * @param projectId
     * @param keyword
     * @return
     */
    public List<UserDTO> getExecUserList(String projectId, String keyword) {
        return extUserMapper.getUserByKeyword(projectId, keyword);
    }

    @Override
    public void associateCollection(String planId, Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates, SessionUser user) {
        List<TestPlanFunctionalCase> testPlanFunctionalCaseList = new ArrayList<>();
        List<BaseCollectionAssociateRequest> functionalList = collectionAssociates.get(AssociateCaseType.FUNCTIONAL);
        if (CollectionUtils.isNotEmpty(functionalList)) {
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
            boolean isRepeat = testPlanConfigService.isRepeatCase(testPlan.getId());
            functionalList.forEach(functional -> buildTestPlanFunctionalCase(testPlan, functional, user, testPlanFunctionalCaseList, isRepeat));
        }
        if (CollectionUtils.isNotEmpty(testPlanFunctionalCaseList)) {
            testPlanFunctionalCaseMapper.batchInsert(testPlanFunctionalCaseList);
        }
    }

    /**
     * 构建测试计划功能用例对象
     *
     * @param testPlan
     * @param functional
     * @param user
     * @param testPlanFunctionalCaseList
     */
    private void buildTestPlanFunctionalCase(TestPlan testPlan, BaseCollectionAssociateRequest functional, SessionUser user, List<TestPlanFunctionalCase> testPlanFunctionalCaseList, boolean isRepeat) {
        super.checkCollection(testPlan.getId(), functional.getCollectionId(), CaseType.FUNCTIONAL_CASE.getKey());
        boolean selectAllModule = functional.getModules().isSelectAllModule();
        List<Map<String, ModuleSelectDTO>> moduleMaps = functional.getModules().getModuleMaps();

        if (selectAllModule) {
            // 选择了全部模块
            List<FunctionalCase> functionalCaseList = extFunctionalCaseMapper.selectAllFunctionalCase(isRepeat, functional.getModules().getProjectId(), testPlan.getId());
            buildTestPlanFunctionalCaseDTO(functional, functionalCaseList, testPlan, user, testPlanFunctionalCaseList);
            handleSyncCase(functionalCaseList, functional, testPlan, user);
        } else {
            AssociateCaseDTO dto = super.getCaseIds(moduleMaps);
            List<FunctionalCase> functionalCaseList = extFunctionalCaseMapper.selectCaseByModules(isRepeat, functional.getModules().getProjectId(), dto, testPlan.getId());
            buildTestPlanFunctionalCaseDTO(functional, functionalCaseList, testPlan, user, testPlanFunctionalCaseList);
            handleSyncCase(functionalCaseList, functional, testPlan, user);
        }
    }


    /**
     * 处理同步添加功能用例关联的用例
     *
     * @param functionalCaseList
     * @param functional
     * @param testPlan
     * @param user
     */
    private void handleSyncCase(List<FunctionalCase> functionalCaseList, BaseCollectionAssociateRequest functional, TestPlan testPlan, SessionUser user) {
        if (BooleanUtils.isTrue(functional.getModules().isSyncCase())) {
            handleApiCaseData(functionalCaseList, functional, testPlan, user);
            handleApiScenarioData(functionalCaseList, functional, testPlan, user);
        }
    }

    /**
     * 处理场景用例数据
     *
     * @param functionalCaseList
     * @param functional
     * @param testPlan
     * @param user
     */
    private void handleApiScenarioData(List<FunctionalCase> functionalCaseList, BaseCollectionAssociateRequest functional, TestPlan testPlan, SessionUser user) {
        if (StringUtils.isNotBlank(functional.getModules().getApiScenarioCollectionId()) && checkApiCollection(testPlan, functional.getModules().getApiScenarioCollectionId(), CaseType.SCENARIO_CASE.getKey())) {
            List<String> caseIds = functionalCaseList.stream().map(FunctionalCase::getId).toList();
            List<ApiScenario> scenarioList = extFunctionalCaseTestMapper.selectApiScenarioByCaseIds(caseIds);
            List<TestPlanApiScenario> testPlanApiScenarioList = new ArrayList<>();
            testPlanApiScenarioService.buildTestPlanApiScenarioDTO(functional, scenarioList, testPlan, user, testPlanApiScenarioList);
            if (CollectionUtils.isNotEmpty(testPlanApiScenarioList)) {
                testPlanApiScenarioMapper.batchInsert(testPlanApiScenarioList);
            }
        }
    }

    /**
     * 处理接口用例数据
     *
     * @param functionalCaseList
     * @param functional
     * @param testPlan
     * @param user
     */
    private void handleApiCaseData(List<FunctionalCase> functionalCaseList, BaseCollectionAssociateRequest functional, TestPlan testPlan, SessionUser user) {
        if (StringUtils.isNotBlank(functional.getModules().getApiCaseCollectionId()) && checkApiCollection(testPlan, functional.getModules().getApiCaseCollectionId(), CaseType.API_CASE.getKey())) {
            List<String> caseIds = functionalCaseList.stream().map(FunctionalCase::getId).toList();
            List<ApiTestCase> apiTestCaseList = extFunctionalCaseTestMapper.selectApiCaseByCaseIds(caseIds);
            List<TestPlanApiCase> testPlanApiCaseList = new ArrayList<>();
            testPlanApiCaseService.buildTestPlanApiCaseDTO(functional, apiTestCaseList, testPlan, user, testPlanApiCaseList);
            if (CollectionUtils.isNotEmpty(testPlanApiCaseList)) {
                testPlanApiCaseMapper.batchInsert(testPlanApiCaseList);
            }
        }
    }

    /**
     * 校验测试集
     *
     * @param testPlan
     * @param apiCaseCollectionId
     * @return
     */
    private boolean checkApiCollection(TestPlan testPlan, String apiCaseCollectionId, String type) {
        TestPlanCollectionExample collectionExample = new TestPlanCollectionExample();
        collectionExample.createCriteria().andIdEqualTo(apiCaseCollectionId).andTestPlanIdEqualTo(testPlan.getId()).andTypeEqualTo(type);
        return testPlanCollectionMapper.countByExample(collectionExample) > 0;
    }

    private void buildTestPlanFunctionalCaseDTO(BaseCollectionAssociateRequest functional, List<FunctionalCase> functionalCaseList, TestPlan testPlan, SessionUser user, List<TestPlanFunctionalCase> testPlanFunctionalCaseList) {
        AtomicLong nextOrder = new AtomicLong(getNextOrder(functional.getCollectionId()));
        functionalCaseList.forEach(functionalCase -> {
            TestPlanFunctionalCase testPlanFunctionalCase = new TestPlanFunctionalCase();
            testPlanFunctionalCase.setId(IDGenerator.nextStr());
            testPlanFunctionalCase.setTestPlanCollectionId(functional.getCollectionId());
            testPlanFunctionalCase.setTestPlanId(testPlan.getId());
            testPlanFunctionalCase.setFunctionalCaseId(functionalCase.getId());
            testPlanFunctionalCase.setCreateUser(user.getId());
            testPlanFunctionalCase.setCreateTime(System.currentTimeMillis());
            testPlanFunctionalCase.setPos(nextOrder.getAndAdd(DEFAULT_NODE_INTERVAL_POS));
            testPlanFunctionalCase.setExecuteUser(functionalCase.getCreateUser());
            testPlanFunctionalCase.setLastExecResult(ExecStatus.PENDING.name());
            testPlanFunctionalCaseList.add(testPlanFunctionalCase);
        });
    }

    @Override
    public void initResourceDefaultCollection(String planId, List<TestPlanCollectionDTO> defaultCollections) {
        TestPlanCollectionDTO defaultCollection = defaultCollections.stream().filter(collection -> StringUtils.equals(collection.getType(), CaseType.FUNCTIONAL_CASE.getKey())
                && !StringUtils.equals(collection.getParentId(), "NONE")).toList().getFirst();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanFunctionalCaseMapper functionalBatchMapper = sqlSession.getMapper(TestPlanFunctionalCaseMapper.class);
        TestPlanFunctionalCase record = new TestPlanFunctionalCase();
        record.setTestPlanCollectionId(defaultCollection.getId());
        TestPlanFunctionalCaseExample functionalCaseExample = new TestPlanFunctionalCaseExample();
        functionalCaseExample.createCriteria().andTestPlanIdEqualTo(planId);
        functionalBatchMapper.updateByExampleSelective(record, functionalCaseExample);
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
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
        TestPlanFunctionalCaseMapper functionalBatchMapper = sqlSession.getMapper(TestPlanFunctionalCaseMapper.class);
        ids.forEach(id -> {
            TestPlanFunctionalCase testPlanFunctionalCase = new TestPlanFunctionalCase();
            testPlanFunctionalCase.setId(id);
            testPlanFunctionalCase.setPos(nextOrder.getAndAdd(DEFAULT_NODE_INTERVAL_POS));
            testPlanFunctionalCase.setTestPlanCollectionId(targetCollectionId);
            functionalBatchMapper.updateByPrimaryKeySelective(testPlanFunctionalCase);
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public Map<String, List<String>> getFuncCaseAssociationCaseMap(List<String> functionalCaseIds) {
        if (CollectionUtils.isEmpty(functionalCaseIds)) {
            return new HashMap<>();
        }
        List<FunctionalCaseTest> functionalCaseTestList = extFunctionalCaseTestMapper.selectApiAndScenarioIdsFromCaseIds(functionalCaseIds);
        return functionalCaseTestList.stream().collect(Collectors.groupingBy(FunctionalCaseTest::getCaseId, Collectors.mapping(FunctionalCaseTest::getSourceId, Collectors.toList())));
    }
}
