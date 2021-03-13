package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.performance.base.ChartsData;
import io.metersphere.track.response.BugStatustics;
import io.metersphere.track.response.TestPlanBugCount;
import io.metersphere.track.response.TrackCountResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TrackService {

    @Resource
    private ExtTestCaseMapper extTestCaseMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanTestCaseMapper testPlanTestCaseMapper;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;

    public List<TrackCountResult> countPriority(String projectId) {
        return extTestCaseMapper.countPriority(projectId);
    }

    public long countCreatedThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extTestCaseMapper.countCreatedThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<TrackCountResult> countStatus(String projectId) {
        return extTestCaseMapper.countStatus(projectId);
    }

    public List<TrackCountResult> countRelevance(String projectId) {
        return extTestCaseMapper.countRelevance(projectId);
    }

    public long countRelevanceCreatedThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extTestCaseMapper.countRelevanceCreatedThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<TrackCountResult> countCoverage(String projectId) {
        return extTestCaseMapper.countCoverage(projectId);
    }

    public List<ChartsData> getCaseMaintenanceBar(String projectId) {
        List<TrackCountResult> funcMaintainer = extTestCaseMapper.countFuncMaintainer(projectId);
        List<TrackCountResult> relevanceMaintainer = extTestCaseMapper.countRelevanceMaintainer(projectId);
        List<String> list = relevanceMaintainer.stream().map(TrackCountResult::getGroupField).collect(Collectors.toList());

        List<ChartsData> charts = new ArrayList<>();
        for (TrackCountResult result : funcMaintainer) {
            String groupField = result.getGroupField();
            if (!list.contains(groupField)) {
                // 创建了功能用例，但是未关联测试
                TrackCountResult trackCount = new TrackCountResult();
                trackCount.setCountNumber(0);
                trackCount.setGroupField(groupField);
                relevanceMaintainer.add(trackCount);
            }
            ChartsData chartsData = new ChartsData();
            chartsData.setxAxis(groupField);
            chartsData.setyAxis(BigDecimal.valueOf(result.getCountNumber()));
            chartsData.setGroupName("FUNCTIONCASE");
            charts.add(chartsData);
        }

        for (TrackCountResult result : relevanceMaintainer) {
            ChartsData chartsData = new ChartsData();
            chartsData.setxAxis(result.getGroupField());
            chartsData.setyAxis(BigDecimal.valueOf(result.getCountNumber()));
            chartsData.setGroupName("RELEVANCECASE");
            charts.add(chartsData);
        }

        return charts;
    }

    public BugStatustics getBugStatustics(String projectId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<TestPlan> plans = testPlanMapper.selectByExample(example);
        List<TestPlanBugCount> list = new ArrayList<>();
        BugStatustics bugStatustics = new BugStatustics();
        int index = 1;
        for (TestPlan plan : plans) {
            TestPlanBugCount testPlanBug = new TestPlanBugCount();
            testPlanBug.setIndex(index++);
            testPlanBug.setPlanName(plan.getName());
            testPlanBug.setCreatTime(plan.getCreateTime());
            testPlanBug.setStatus(plan.getStatus());
            testPlanBug.setCaseSize(getPlanCaseSize(plan.getId()));
            testPlanBug.setBugSize(getPlanBugSize(plan.getId()));
            testPlanBug.setPassRage(getPlanPassRage(plan.getId()));
            list.add(testPlanBug);
        }

        // todo
        bugStatustics.setList(list);
        bugStatustics.setRage("1");
        bugStatustics.setBugTotalSize(2);
        return bugStatustics;
    }

    private int getPlanCaseSize(String planId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(planId);
        List<TestPlanTestCase> testPlanTestCases = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample);

        TestPlanApiCaseExample testPlanApiCaseExample = new TestPlanApiCaseExample();
        testPlanApiCaseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(testPlanApiCaseExample);

        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanLoadCase> testPlanLoadCases = testPlanLoadCaseMapper.selectByExample(example);

        TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
        testPlanApiCaseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExample(testPlanApiScenarioExample);


        return testPlanTestCases.size() + testPlanApiCases.size() + testPlanLoadCases.size() + testPlanApiScenarios.size();

    }

    private int getPlanBugSize(String planId) {
        return 1;
    }

    private String getPlanPassRage(String planId) {
        return "10%";
    }
}
