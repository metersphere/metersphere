package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.plan.dto.AssociationNodeSortDTO;
import io.metersphere.plan.dto.request.ResourceSortRequest;
import io.metersphere.plan.dto.request.TestPlanAssociationRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.mapper.ExtTestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.TestPlanResourceConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanFunctionalCaseService extends TestPlanResourceService {
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private ExtTestPlanFunctionalCaseMapper extTestPlanFunctionalCaseMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanResourceLogService testPlanResourceLogService;
    @Resource
    private TestPlanMapper testPlanMapper;

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
            return maxPos + DEFAULT_NODE_INTERVAL_POS;
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

    public TestPlanAssociationResponse association(TestPlanAssociationRequest request, String operator, String requestUrl, String requestMethod) {
        TestPlanAssociationResponse response = new TestPlanAssociationResponse();
        if (request.isEmpty()) {
            return response;
        }
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(request.getTestPlanId());
        boolean repeatCase = testPlanConfig.getRepeatCase();
        List<String> associationIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getSelectIds())) {
            //获取有效ID
            associationIdList.addAll(extTestPlanFunctionalCaseMapper.getIdByIds(request.getSelectIds(), request.getTestPlanId(), repeatCase, request.getOrderString()));
        }
        if (CollectionUtils.isNotEmpty(request.getSelectModuleIds())) {
            //获取有效ID
            associationIdList.addAll(extTestPlanFunctionalCaseMapper.getIdByModuleIds(request.getSelectModuleIds(), request.getTestPlanId(), repeatCase, request.getOrderString()));
        }
        associationIdList = new ArrayList<>(associationIdList.stream().distinct().toList());
        associationIdList.removeAll(request.getExcludeIds());

        this.saveTestPlanResource(associationIdList, request.getTestPlanId(), operator);
        response.setAssociationCount(associationIdList.size());

        testPlanResourceLogService.saveAddLog(testPlan, TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE, operator, requestUrl, requestMethod);
        return response;
    }

    private void saveTestPlanResource(List<String> associationIdList, String testPlanId, String operator) {
        if (CollectionUtils.isEmpty(associationIdList)) {
            return;
        }
        long pox = this.getNextOrder(testPlanId);
        long now = System.currentTimeMillis();
        List<TestPlanFunctionalCase> testPlanFunctionalCaseList = new ArrayList<>();
        for (int i = 0; i < associationIdList.size(); i++) {
            TestPlanFunctionalCase testPlanFunctionalCase = new TestPlanFunctionalCase();
            testPlanFunctionalCase.setId(IDGenerator.nextStr());
            testPlanFunctionalCase.setTestPlanId(testPlanId);
            testPlanFunctionalCase.setFunctionalCaseId(associationIdList.get(i));
            testPlanFunctionalCase.setPos(pox + (i + 1) * DEFAULT_NODE_INTERVAL_POS);
            testPlanFunctionalCase.setCreateTime(now);
            testPlanFunctionalCase.setCreateUser(operator);
            testPlanFunctionalCase.setExecuteUser(operator);
            testPlanFunctionalCaseList.add(testPlanFunctionalCase);
        }
        testPlanFunctionalCaseMapper.batchInsert(testPlanFunctionalCaseList);
    }

    public TestPlanResourceSortResponse sortNode(ResourceSortRequest request, String userId, String requestUrl, String requestMethod) {
        TestPlanResourceSortResponse response = new TestPlanResourceSortResponse();
        if (request.getDropPosition() == -1 || request.getDropPosition() == 1) {
            TestPlanFunctionalCase dragNode = testPlanFunctionalCaseMapper.selectByPrimaryKey(request.getDragNodeId());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
            if (dragNode == null && testPlan == null) {
                throw new MSException("test_plan.drag.node.error");
            }
            AssociationNodeSortDTO sortDTO = super.getNodeSortDTO(
                    request,
                    extTestPlanFunctionalCaseMapper::selectDragInfoById,
                    extTestPlanFunctionalCaseMapper::selectNodeByPosOperator
            );
            this.sort(sortDTO);
            response.setSortNodeNum(1);
            testPlanResourceLogService.saveSortLog(testPlan, request.getDragNodeId(), TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE, userId, requestUrl, requestMethod);
        } else {
            throw new MSException("test_plan.drag.position.error");
        }
        return response;
    }
}
