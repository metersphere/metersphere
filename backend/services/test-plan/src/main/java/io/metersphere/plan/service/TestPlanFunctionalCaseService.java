package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.plan.dto.AssociationNodeSortDTO;
import io.metersphere.plan.dto.ResourceLogInsertModule;
import io.metersphere.plan.dto.TestPlanResourceAssociationParam;
import io.metersphere.plan.dto.request.ResourceSortRequest;
import io.metersphere.plan.dto.request.TestPlanAssociationRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.mapper.ExtTestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.TestPlanResourceConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanResourceLogService testPlanResourceLogService;
    @Resource
    private TestPlanMapper testPlanMapper;

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

    public TestPlanAssociationResponse association(TestPlanAssociationRequest request, LogInsertModule logInsertModule) {
        return super.association(
                TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE,
                request,
                logInsertModule,
                extTestPlanFunctionalCaseMapper::getIdByParam,
                this::saveTestPlanResource);
    }

    private void saveTestPlanResource(@Validated TestPlanResourceAssociationParam associationParam) {
        long pox = this.getNextOrder(associationParam.getTestPlanId());
        long now = System.currentTimeMillis();
        List<TestPlanFunctionalCase> testPlanFunctionalCaseList = new ArrayList<>();
        List<String> associationIdList = associationParam.getResourceIdList();
        for (int i = 0; i < associationIdList.size(); i++) {
            TestPlanFunctionalCase testPlanFunctionalCase = new TestPlanFunctionalCase();
            testPlanFunctionalCase.setId(IDGenerator.nextStr());
            testPlanFunctionalCase.setNum(NumGenerator.nextNum(associationParam.getTestPlanNum() + "_" + associationParam.getProjectId(), ApplicationNumScope.TEST_PLAN_FUNCTION_CASE));
            testPlanFunctionalCase.setTestPlanId(associationParam.getTestPlanId());
            testPlanFunctionalCase.setFunctionalCaseId(associationIdList.get(i));
            testPlanFunctionalCase.setPos(pox + (i + 1) * DEFAULT_NODE_INTERVAL_POS);
            testPlanFunctionalCase.setCreateTime(now);
            testPlanFunctionalCase.setCreateUser(associationParam.getOperator());
            testPlanFunctionalCase.setExecuteUser(associationParam.getOperator());
            testPlanFunctionalCaseList.add(testPlanFunctionalCase);
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
}
