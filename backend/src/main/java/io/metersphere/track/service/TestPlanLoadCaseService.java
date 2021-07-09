package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.commons.constants.RunModeConstants;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.request.testplan.LoadCaseReportBatchRequest;
import io.metersphere.track.request.testplan.LoadCaseReportRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testplan.RunBatchTestPlanRequest;
import io.metersphere.track.service.utils.ParallelExecTask;
import io.metersphere.track.service.utils.SerialExecTask;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanLoadCaseService {
    @Resource
    TestPlanMapper testPlanMapper;
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
        List<String> ids = extTestPlanLoadCaseMapper.selectIdsNotInPlan(request);
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

    public List<String> selectTestPlanLoadCaseIds(LoadCaseRequest request) {
        List<OrderRequest> orders = request.getOrders();
        if (orders == null || orders.size() < 1) {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setName("create_time");
            orderRequest.setType("desc");
            orders = new ArrayList<>();
            orders.add(orderRequest);
        }
        request.setOrders(orders);
        return extTestPlanLoadCaseMapper.selectTestPlanLoadCaseId(request);
    }

    public void relevanceCase(LoadCaseRequest request) {
        List<String> caseIds = request.getCaseIds();
        String planId = request.getTestPlanId();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

        TestPlanLoadCaseMapper testPlanLoadCaseMapper = sqlSession.getMapper(TestPlanLoadCaseMapper.class);
        caseIds.forEach(id -> {
            TestPlanLoadCase t = new TestPlanLoadCase();
            t.setId(UUID.randomUUID().toString());
            t.setCreateUser(SessionUtils.getUserId());
            t.setTestPlanId(planId);
            t.setLoadCaseId(id);
            t.setCreateTime(System.currentTimeMillis());
            t.setUpdateTime(System.currentTimeMillis());
            testPlanLoadCaseMapper.insert(t);
        });
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        if (org.apache.commons.lang3.StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Prepare.name())
                || org.apache.commons.lang3.StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Completed.name())) {
            testPlan.setStatus(TestPlanStatus.Underway.name());
            testPlan.setActualStartTime(System.currentTimeMillis());  // 将状态更新为进行中时，开始时间也要更新
            testPlan.setActualEndTime(null);
            testPlanMapper.updateByPrimaryKey(testPlan);
        }
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

    public void runBatch(RunBatchTestPlanRequest request) {
        try {
            if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                serialRun(request);
            } else {
                ExecutorService executorService = Executors.newFixedThreadPool(request.getRequests().size());
                request.getRequests().forEach(item -> {
                    executorService.submit(new ParallelExecTask(performanceTestService, testPlanLoadCaseMapper, item));
                });
            }
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(e.getMessage())) {
                MSException.throwException("测试正在运行, 请等待！");
            } else {
                MSException.throwException("请求参数错误，请刷新后执行！");
            }
        }
    }

    private void serialRun(RunBatchTestPlanRequest request) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(request.getRequests().size());
        for (RunTestPlanRequest runTestPlanRequest : request.getRequests()) {
            Future<LoadTestReportWithBLOBs> future = executorService.submit(new SerialExecTask(performanceTestService, testPlanLoadCaseMapper, loadTestReportMapper, runTestPlanRequest, request.getConfig()));
            LoadTestReportWithBLOBs report = future.get();
            // 如果开启失败结束执行，则判断返回结果状态
            if (request.getConfig().isOnSampleError()) {
                TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
                example.createCriteria().andLoadReportIdEqualTo(report.getId());
                List<TestPlanLoadCase> cases = testPlanLoadCaseMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(cases) || !cases.get(0).getStatus().equals("success")) {
                    break;
                }
            }
        }
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

    public void batchDelete(LoadCaseReportBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectTestPlanLoadCaseIds(request.getCondition());
            if (request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }

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

    public void updateByApi(TestPlanLoadCase testPlanLoadCase) {
        String testPlanId = testPlanLoadCase.getTestPlanId();
        String loadCaseId = testPlanLoadCase.getLoadCaseId();
        String status = testPlanLoadCase.getStatus();
        extTestPlanLoadCaseMapper.updateCaseStatusByApi(testPlanId, loadCaseId, status);
    }

    public List<String> getStatus(String planId) {
        return extTestPlanLoadCaseMapper.getStatusByTestPlanId(planId);
    }

    public List<TestPlanLoadCaseDTO> selectAllTableRows(LoadCaseReportBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectTestPlanLoadCaseIds(request.getCondition());
            if (request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }

        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<OrderRequest> orders = request.getCondition().getOrders();
        if (orders == null || orders.size() < 1) {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setName("create_time");
            orderRequest.setType("desc");
            orders = new ArrayList<>();
            orders.add(orderRequest);
        }

        LoadCaseRequest tableReq = new LoadCaseRequest();
        tableReq.setIds(ids);
        tableReq.setOrders(orders);
        List<TestPlanLoadCaseDTO> list = extTestPlanLoadCaseMapper.selectByIdIn(tableReq);
        return list;
    }

    public String getLogDetails(String id) {
        TestPlanLoadCase bloBs = testPlanLoadCaseMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(bloBs.getTestPlanId());
            LoadTest test = loadTestMapper.selectByPrimaryKey(bloBs.getLoadCaseId());
            if (test != null && testPlan != null) {
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testPlan.getProjectId(), test.getName(), bloBs.getCreateUser(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanLoadCaseExample caseExample = new TestPlanLoadCaseExample();
        caseExample.createCriteria().andIdIn(ids);
        List<TestPlanLoadCase> cases = testPlanLoadCaseMapper.selectByExample(caseExample);
        if (CollectionUtils.isNotEmpty(cases)) {
            LoadTestExample example = new LoadTestExample();
            example.createCriteria().andIdIn(cases.stream().map(TestPlanLoadCase::getLoadCaseId).collect(Collectors.toList()));
            List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
            List<String> names = loadTests.stream().map(LoadTest::getName).collect(Collectors.toList());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(cases.get(0).getTestPlanId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), testPlan.getProjectId(), String.join(",", names), testPlan.getCreator(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids, String planId) {
        TestPlanLoadCaseExample caseExample = new TestPlanLoadCaseExample();
        caseExample.createCriteria().andLoadCaseIdIn(ids).andTestPlanIdEqualTo(planId);
        List<TestPlanLoadCase> cases = testPlanLoadCaseMapper.selectByExample(caseExample);
        if (CollectionUtils.isNotEmpty(cases)) {
            LoadTestExample example = new LoadTestExample();
            example.createCriteria().andIdIn(cases.stream().map(TestPlanLoadCase::getLoadCaseId).collect(Collectors.toList()));
            List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(loadTests)) {
                List<String> names = loadTests.stream().map(LoadTest::getName).collect(Collectors.toList());
                TestPlan testPlan = testPlanMapper.selectByPrimaryKey(cases.get(0).getTestPlanId());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), testPlan.getProjectId(), String.join(",", names), testPlan.getCreator(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public String getRunLogDetails(List<RunTestPlanRequest> requests) {
        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andIdIn(requests.stream().map(RunTestPlanRequest::getId).collect(Collectors.toList()));
        List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(loadTests)) {
            List<String> ids = loadTests.stream().map(LoadTest::getId).collect(Collectors.toList());
            List<String> names = loadTests.stream().map(LoadTest::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), loadTests.get(0).getProjectId(), String.join(",", names), null, new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public Boolean hasFailCase(String planId, List<String> performanceIds) {
        if (CollectionUtils.isEmpty(performanceIds)) {
            return false;
        }
        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andLoadCaseIdIn(performanceIds)
                .andStatusEqualTo("error");
        return testPlanLoadCaseMapper.countByExample(example) > 0 ? true : false;
    }
}
