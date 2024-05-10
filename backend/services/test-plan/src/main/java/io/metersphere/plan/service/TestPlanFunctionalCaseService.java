package io.metersphere.plan.service;

import io.metersphere.bug.dto.CaseRelateBugDTO;
import io.metersphere.bug.mapper.ExtBugRelateCaseMapper;
import io.metersphere.functional.dto.FunctionalCaseCustomFieldDTO;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.plan.dto.AssociationNodeSortDTO;
import io.metersphere.plan.dto.ResourceLogInsertModule;
import io.metersphere.plan.dto.TestPlanResourceAssociationParam;
import io.metersphere.plan.dto.request.ResourceSortRequest;
import io.metersphere.plan.dto.request.TestPlanAssociationRequest;
import io.metersphere.plan.dto.request.TestPlanCaseRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanCasePageResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.mapper.ExtTestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.TestPlanResourceConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Override
    public int deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        return testPlanFunctionalCaseMapper.deleteByExample(example);
    }

    @Override
    public long getNextOrder(String testPlanId) {
        Long maxPos = extTestPlanFunctionalCaseMapper.getMaxPosByTestPlanId(testPlanId);
        if (maxPos == null) {
            return 0;
        } else {
            return maxPos + ServiceUtils.POS_STEP;
        }
    }

    @Override
    public void updatePos(String id, long pos) {
        extTestPlanFunctionalCaseMapper.updatePos(id, pos);
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

    public TestPlanAssociationResponse association(TestPlanAssociationRequest request, LogInsertModule logInsertModule) {
        return super.association(
                TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE,
                request,
                logInsertModule,
                extTestPlanFunctionalCaseMapper::getIdByParam,
                this::saveTestPlanResource);
    }

    public void saveTestPlanResource(@Validated TestPlanResourceAssociationParam associationParam) {
        long pox = this.getNextOrder(associationParam.getTestPlanId());
        long now = System.currentTimeMillis();
        List<TestPlanFunctionalCase> testPlanFunctionalCaseList = new ArrayList<>();
        List<String> associationIdList = associationParam.getResourceIdList();
        for (int i = 0; i < associationIdList.size(); i++) {
            TestPlanFunctionalCase testPlanFunctionalCase = new TestPlanFunctionalCase();
            testPlanFunctionalCase.setId(IDGenerator.nextStr());
            testPlanFunctionalCase.setTestPlanId(associationParam.getTestPlanId());
            testPlanFunctionalCase.setFunctionalCaseId(associationIdList.get(i));
            testPlanFunctionalCase.setPos(pox);
            testPlanFunctionalCase.setCreateTime(now);
            testPlanFunctionalCase.setCreateUser(associationParam.getOperator());
            testPlanFunctionalCaseList.add(testPlanFunctionalCase);
            pox += ServiceUtils.POS_STEP;
        }
        testPlanFunctionalCaseMapper.batchInsert(testPlanFunctionalCaseList);
    }

    public TestPlanResourceSortResponse sortNode(ResourceSortRequest request, LogInsertModule logInsertModule) {
        TestPlanFunctionalCase dragNode = testPlanFunctionalCaseMapper.selectByPrimaryKey(request.getDragNodeId());
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        if (dragNode == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        TestPlanResourceSortResponse response = new TestPlanResourceSortResponse();
        AssociationNodeSortDTO sortDTO = super.getNodeSortDTO(
                request,
                extTestPlanFunctionalCaseMapper::selectDragInfoById,
                extTestPlanFunctionalCaseMapper::selectNodeByPosOperator
        );
        super.sort(sortDTO);
        response.setSortNodeNum(1);
        testPlanResourceLogService.saveSortLog(testPlan, request.getDragNodeId(), new ResourceLogInsertModule(TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE, logInsertModule));
        return response;
    }


    /**
     * 复制计划时，复制功能用例
     *
     * @param ids
     * @param testPlan
     */
    public void saveTestPlanByPlanId(List<String> ids, TestPlan testPlan) {
        TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
        example.createCriteria().andTestPlanIdIn(ids);
        List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(testPlanFunctionalCases)) {
            Map<String, List<TestPlanFunctionalCase>> collect = testPlanFunctionalCases.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getTestPlanId));
            List<TestPlanFunctionalCase> associateList = new ArrayList<>();
            ids.forEach(id -> {
                if (collect.containsKey(id)) {
                    saveCase(collect.get(id), associateList, testPlan, id);
                }
            });
            testPlanFunctionalCaseMapper.batchInsert(associateList);
        }
    }

    private void saveCase(List<TestPlanFunctionalCase> testPlanFunctionalCases, List<TestPlanFunctionalCase> associateList, TestPlan testPlan, String id) {
        AtomicLong pos = new AtomicLong(this.getNextOrder(id));
        testPlanFunctionalCases.forEach(item -> {
            TestPlanFunctionalCase functionalCase = new TestPlanFunctionalCase();
            functionalCase.setTestPlanId(testPlan.getId());
            functionalCase.setId(IDGenerator.nextStr());
            functionalCase.setCreateTime(System.currentTimeMillis());
            functionalCase.setCreateUser(testPlan.getCreateUser());
            functionalCase.setFunctionalCaseId(item.getFunctionalCaseId());
            functionalCase.setPos(pos.get());
            associateList.add(functionalCase);
            pos.updateAndGet(v -> v + ServiceUtils.POS_STEP);
        });
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
        Map<String, List<CaseRelateBugDTO>> bugListMap = getBugData(ids);
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));
        functionalCaseLists.forEach(testPlanCasePageResponse -> {
            testPlanCasePageResponse.setCustomFields(collect.get(testPlanCasePageResponse.getCaseId()));
            testPlanCasePageResponse.setCreateUserName(userMap.get(testPlanCasePageResponse.getCreateUser()));
            testPlanCasePageResponse.setExecuteUserName(userMap.get(testPlanCasePageResponse.getExecuteUser()));
            if (bugListMap.containsKey(testPlanCasePageResponse.getCaseId())) {
                List<CaseRelateBugDTO> bugDTOList = bugListMap.get(testPlanCasePageResponse.getCaseId());
                testPlanCasePageResponse.setBugList(bugDTOList);
                testPlanCasePageResponse.setBugCount(bugDTOList.size());
            }
        });
        return functionalCaseLists;

    }

    private Map<String, List<CaseRelateBugDTO>> getBugData(List<String> ids) {
        List<CaseRelateBugDTO> bugList = bugRelateCaseMapper.getBugCountByIds(ids);
        return bugList.stream().collect(Collectors.groupingBy(CaseRelateBugDTO::getCaseId));
    }


    public Set<String> extractUserIds(List<TestPlanCasePageResponse> list) {
        return list.stream()
                .flatMap(testPlanCasePageResponse -> Stream.of(testPlanCasePageResponse.getUpdateUser(), testPlanCasePageResponse.getCreateUser(), testPlanCasePageResponse.getExecuteUser()))
                .collect(Collectors.toSet());
    }
}
