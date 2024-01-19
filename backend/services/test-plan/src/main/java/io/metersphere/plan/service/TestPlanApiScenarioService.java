package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.domain.TestPlanApiScenarioExample;
import io.metersphere.plan.dto.AssociationNodeSortDTO;
import io.metersphere.plan.dto.LogInsertModule;
import io.metersphere.plan.dto.ResourceLogInsertModule;
import io.metersphere.plan.dto.TestPlanResourceAssociationParam;
import io.metersphere.plan.dto.request.ResourceSortRequest;
import io.metersphere.plan.dto.request.TestPlanAssociationRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.mapper.*;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.TestPlanResourceConstants;
import io.metersphere.sdk.exception.MSException;
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
public class TestPlanApiScenarioService extends TestPlanResourceService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanResourceLogService testPlanResourceLogService;
    @Resource
    private TestPlanMapper testPlanMapper;

    @Override
    public int deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        return testPlanApiScenarioMapper.deleteByExample(example);
    }

    @Override
    public long getNextOrder(String testPlanId) {
        Long maxPos = extTestPlanApiScenarioMapper.getMaxPosByTestPlanId(testPlanId);
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

    public TestPlanAssociationResponse association(TestPlanAssociationRequest request, LogInsertModule logInsertModule) {
        return super.association(
                TestPlanResourceConstants.RESOURCE_API_CASE,
                request,
                logInsertModule,
                extTestPlanApiScenarioMapper::getIdByIds,
                extTestPlanApiScenarioMapper::getIdByModuleIds,
                this::saveTestPlanResource);
    }

    private void saveTestPlanResource(@Validated TestPlanResourceAssociationParam associationParam) {
        long pox = this.getNextOrder(associationParam.getTestPlanId());
        long now = System.currentTimeMillis();
        List<TestPlanApiScenario> testPlanResourceList = new ArrayList<>();
        List<String> associationIdList = associationParam.getResourceIdList();
        for (int i = 0; i < associationIdList.size(); i++) {
            TestPlanApiScenario testPlanResourceCase = new TestPlanApiScenario();
            testPlanResourceCase.setId(IDGenerator.nextStr());
            testPlanResourceCase.setNum(NumGenerator.nextNum(associationParam.getTestPlanNum() + "_" + associationParam.getProjectId(), ApplicationNumScope.TEST_PLAN_API_SCENARIO));
            testPlanResourceCase.setTestPlanId(associationParam.getTestPlanId());
            testPlanResourceCase.setApiScenarioId(associationIdList.get(i));
            testPlanResourceCase.setPos(pox + (i + 1) * DEFAULT_NODE_INTERVAL_POS);
            testPlanResourceCase.setCreateTime(now);
            testPlanResourceCase.setCreateUser(associationParam.getOperator());
            testPlanResourceCase.setExecuteUser(associationParam.getOperator());
            testPlanResourceList.add(testPlanResourceCase);
        }
        testPlanApiScenarioMapper.batchInsert(testPlanResourceList);
    }

    public TestPlanResourceSortResponse sortNode(ResourceSortRequest request, LogInsertModule logInsertModule) {
        TestPlanResourceSortResponse response = new TestPlanResourceSortResponse();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        TestPlanApiScenario dragNode = testPlanApiScenarioMapper.selectByPrimaryKey(request.getDragNodeId());
        if (dragNode == null && testPlan == null) {
            throw new MSException("test_plan.drag.node.error");
        }
        if (request.getDropPosition() == -1 || request.getDropPosition() == 1) {
            AssociationNodeSortDTO sortDTO = super.getNodeSortDTO(
                    request,
                    extTestPlanApiScenarioMapper::selectDragInfoById,
                    extTestPlanApiScenarioMapper::selectNodeByPosOperator
            );
            this.sort(sortDTO);
            response.setSortNodeNum(1);
            testPlanResourceLogService.saveSortLog(testPlan, request.getDragNodeId(), new ResourceLogInsertModule(TestPlanResourceConstants.RESOURCE_API_CASE, logInsertModule));
        } else {
            throw new MSException("test_plan.drag.position.error");
        }
        return response;
    }
}
