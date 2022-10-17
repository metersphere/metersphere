package io.metersphere.service;

import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.domain.TestPlanExample;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.dto.BugStatustics;
import io.metersphere.dto.TestPlanBugCount;
import io.metersphere.dto.TestPlanDTOWithMetric;
import io.metersphere.dto.TrackCountResult;
import io.metersphere.plan.dto.ChartsData;
import io.metersphere.plan.service.TestPlanService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class TrackService {

    @Resource
    private ExtTestCaseMapper extTestCaseMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private CustomFieldIssuesService customFieldIssuesService;
    @Resource
    private TestPlanService testPlanService;

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

    public int countCoverage(String projectId) {
        return extTestCaseMapper.countCoverage(projectId);
    }

    public List<ChartsData> getCaseMaintenanceBar(String projectId) {
        List<TrackCountResult> funcMaintainer = extTestCaseMapper.countFuncMaintainer(projectId);
        List<TrackCountResult> relevanceMaintainer = extTestCaseMapper.countRelevanceMaintainer(projectId);

        List<ChartsData> charts = new ArrayList<>();
        for (TrackCountResult result : funcMaintainer) {
            String groupField = result.getGroupField();
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

    public BugStatustics getBugStatistics(String projectId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<TestPlan> plans = testPlanMapper.selectByExample(example);
        List<TestPlanBugCount> list = new ArrayList<>();
        BugStatustics bugStatustics = new BugStatustics();
        int index = 1;
        int totalCaseSize = 0;
        int totalBugSize = 0;
        for (TestPlan plan : plans) {
            int planBugSize = getPlanBugSize(plan.getId(), projectId);
            // bug为0不记录
            if (planBugSize == 0) {
                continue;
            }
            totalBugSize += planBugSize;

            TestPlanBugCount testPlanBug = new TestPlanBugCount();
            testPlanBug.setIndex(index++);
            testPlanBug.setPlanName(plan.getName());
            testPlanBug.setCreateTime(plan.getCreateTime());
            testPlanBug.setStatus(plan.getStatus());
            testPlanBug.setPlanId(plan.getId());

            int planCaseSize = getPlanCaseSize(plan.getId());
            testPlanBug.setCaseSize(planCaseSize);

            testPlanBug.setBugSize(planBugSize);
            double planPassRage = getPlanPassRage(plan.getId());
            testPlanBug.setPassRage(planPassRage + "%");
            list.add(testPlanBug);
            totalCaseSize += planCaseSize;

        }
        bugStatustics.setList(list);
        float rage = totalCaseSize == 0 ? 0 : (float) totalBugSize * 100 / totalCaseSize;
        DecimalFormat df = new DecimalFormat("0.0");
        bugStatustics.setRage(df.format(rage) + "%");
        bugStatustics.setBugTotalSize(totalBugSize);
        return bugStatustics;
    }

    private int getPlanCaseSize(String planId) {
        return extTestCaseMapper.getTestPlanCase(planId);

    }

    private int getPlanBugSize(String planId, String projectId) {
        List<String> issueIds = extTestCaseMapper.getTestPlanBug(planId);

        Map<String, String> statusMap = customFieldIssuesService.getIssueStatusMap(issueIds, projectId);

        // 缺陷是否有状态
        if (MapUtils.isEmpty(statusMap)) {
            return issueIds.size();
        }
        return (int) issueIds.stream()
                .filter(id -> !StringUtils.equals(statusMap.getOrDefault(id, StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY), "closed"))
                .count();
    }

    private double getPlanPassRage(String planId) {
        TestPlanDTOWithMetric testPlan = new TestPlanDTOWithMetric();
        testPlan.setId(planId);
        testPlanService.calcTestPlanRate(testPlan);
        return testPlan.getPassRate();
    }
}
