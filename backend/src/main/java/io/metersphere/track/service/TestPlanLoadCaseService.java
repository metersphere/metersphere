package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.request.testplan.LoadCaseReportRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testplan.RunTestPlanRequest;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanLoadCaseService {

    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private LoadTestMapper loadTestMapper;

    public List<LoadTest> relevanceList(LoadCaseRequest request) {
        List<String> ids = extTestPlanLoadCaseMapper.selectIdsNotInPlan(request.getProjectId(), request.getTestPlanId());
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return performanceTestService.getLoadTestListByIds(ids);
    }

    public List<TestPlanLoadCaseDTO> list(LoadCaseRequest request) {
        List<OrderRequest> orders = request.getOrders();
        if (orders == null || orders.size() < 1) {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setName("create_time");
            orderRequest.setType("desc");
            orders = new ArrayList<>();
            orders.add(orderRequest);
        }
        request.setOrders(orders);
        return extTestPlanLoadCaseMapper.selectTestPlanLoadCaseList(request);
    }

    public void relevanceCase(LoadCaseRequest request) {
        List<String> caseIds = request.getCaseIds();
        String planId = request.getTestPlanId();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

        TestPlanLoadCaseMapper testPlanLoadCaseMapper = sqlSession.getMapper(TestPlanLoadCaseMapper.class);
        caseIds.forEach(id -> {
            TestPlanLoadCase t = new TestPlanLoadCase();
            t.setId(UUID.randomUUID().toString());
            t.setTestPlanId(planId);
            t.setLoadCaseId(id);
            t.setCreateTime(System.currentTimeMillis());
            t.setUpdateTime(System.currentTimeMillis());
            testPlanLoadCaseMapper.insert(t);
        });
        sqlSession.flushStatements();
    }

    public void delete(String id) {
        TestPlanLoadCaseExample testPlanLoadCaseExample = new TestPlanLoadCaseExample();
        testPlanLoadCaseExample.createCriteria().andIdEqualTo(id);
        testPlanLoadCaseMapper.deleteByExample(testPlanLoadCaseExample);
    }

    public String run(RunTestPlanRequest request) {
        String reportId = performanceTestService.run(request);
        TestPlanLoadCase testPlanLoadCase = new TestPlanLoadCase();
        testPlanLoadCase.setId(request.getTestPlanLoadId());
        testPlanLoadCase.setLoadReportId(reportId);
        testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);
        return reportId;
    }

    public Boolean isExistReport(LoadCaseReportRequest request) {
        String reportId = request.getReportId();
        String testPlanLoadCaseId = request.getTestPlanLoadCaseId();
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andIdEqualTo(reportId);
        List<LoadTestReport> loadTestReports = loadTestReportMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(loadTestReports)) {
            TestPlanLoadCase testPlanLoadCase = new TestPlanLoadCase();
            testPlanLoadCase.setId(testPlanLoadCaseId);
            testPlanLoadCase.setLoadReportId("");
            testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);
            return false;
        }
        return true;
    }

    public void deleteByRelevanceProjectIds(String id, List<String> relevanceProjectIds) {
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andProjectIdIn(relevanceProjectIds);
        List<LoadTest> loadTests = loadTestMapper.selectByExample(loadTestExample);
        TestPlanLoadCaseExample testPlanLoadCaseExample = new TestPlanLoadCaseExample();
        TestPlanLoadCaseExample.Criteria criteria = testPlanLoadCaseExample.createCriteria().andTestPlanIdEqualTo(id);
        if (!CollectionUtils.isEmpty(loadTests)) {
            List<String> ids = loadTests.stream().map(LoadTest::getId).collect(Collectors.toList());
            criteria.andLoadCaseIdNotIn(ids);
        }
        testPlanLoadCaseMapper.deleteByExample(testPlanLoadCaseExample);
    }

    public void batchDelete(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria().andIdIn(ids);
        testPlanLoadCaseMapper.deleteByExample(example);
    }

    public void update(TestPlanLoadCase testPlanLoadCase) {
        if (!StringUtils.isEmpty(testPlanLoadCase.getId())) {
            testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);
        }
    }

    public List<String> getStatus(String planId) {
        return extTestPlanLoadCaseMapper.getStatusByTestPlanId(planId);
    }
}
