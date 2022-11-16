package io.metersphere.service;

import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.domain.TestPlanExample;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.dto.BugStatistics;
import io.metersphere.dto.TestPlanBugCount;
import io.metersphere.dto.TestPlanDTOWithMetric;
import io.metersphere.dto.TrackCountResult;
import io.metersphere.plan.dto.ChartsData;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.xpack.track.dto.IssuesDao;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private ExtIssuesMapper extIssuesMapper;

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

    public BugStatistics getBugStatistics(String projectId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<TestPlan> plans = testPlanMapper.selectByExample(example);
        List<TestPlanBugCount> list = new ArrayList<>();
        BugStatistics bugStatistics = new BugStatistics();
        int index = 1;
        int totalUnClosedPlanBugSize = 0;
        int totalPlanBugSize = 0;
        int newCount = 0;
        int resolvedCount = 0;
        int rejectedCount = 0;
        int unKnownCount = 0;
        int thisWeekCount = 0;
        for (TestPlan plan : plans) {
            Map<String, Integer> bugSizeMap = getPlanBugSize(plan.getId(), projectId);
            int planBugSize = bugSizeMap.get("total");
            int unClosedPlanBugSize = bugSizeMap.get("unClosed");
            newCount += bugSizeMap.get("newCount");
            resolvedCount += bugSizeMap.get("resolvedCount");
            rejectedCount += bugSizeMap.get("rejectedCount");
            unKnownCount += bugSizeMap.get("unKnownCount");
            thisWeekCount += bugSizeMap.get("thisWeekCount");
            totalUnClosedPlanBugSize += unClosedPlanBugSize;
            totalPlanBugSize += planBugSize;
            // bug为0不记录
            if (unClosedPlanBugSize == 0) {
                continue;
            }

            TestPlanBugCount testPlanBug = new TestPlanBugCount();
            testPlanBug.setIndex(index++);
            testPlanBug.setPlanName(plan.getName());
            testPlanBug.setCreateTime(plan.getCreateTime());
            testPlanBug.setStatus(plan.getStatus());
            testPlanBug.setPlanId(plan.getId());
            testPlanBug.setCaseSize(getPlanCaseSize(plan.getId()));
            testPlanBug.setBugSize(unClosedPlanBugSize);
            double planPassRage = getPlanPassRage(plan.getId());
            testPlanBug.setPassRage(planPassRage + "%");
            list.add(testPlanBug);
        }
        bugStatistics.setList(list);
        bugStatistics.setBugUnclosedCount(totalUnClosedPlanBugSize);
        bugStatistics.setBugTotalCount(totalPlanBugSize);

        float rage = totalPlanBugSize == 0 ? 0 : (float) totalUnClosedPlanBugSize * 100 / totalPlanBugSize;
        DecimalFormat df = new DecimalFormat("0.0");
        bugStatistics.setUnClosedRage(df.format(rage) + "%");

        bugStatistics.setNewCount(newCount);
        bugStatistics.setResolvedCount(resolvedCount);
        bugStatistics.setRejectedCount(rejectedCount);
        bugStatistics.setUnKnownCount(unKnownCount);
        bugStatistics.setThisWeekCount(thisWeekCount);
        return bugStatistics;
    }

    private int getPlanCaseSize(String planId) {
        return extTestCaseMapper.getTestPlanCase(planId);

    }

    private Map<String, Integer> getPlanBugSize(String planId, String projectId) {
        List<String> issueIds = extTestCaseMapper.getTestPlanBug(planId);
        Map<String, String> statusMap = customFieldIssuesService.getIssueStatusMap(issueIds, projectId);
        Map<String, Integer> bugSizeMap = new HashMap<>();
        bugSizeMap.put("total", issueIds.size());

        // 缺陷是否有状态
        List<String> unClosedIds;
        if (MapUtils.isEmpty(statusMap)) {
            unClosedIds = issueIds;
            bugSizeMap.put("unClosed", issueIds.size());
            bugSizeMap.put("newCount", issueIds.size());
        } else {
            unClosedIds = issueIds.stream()
                    .filter(id -> !StringUtils.equals(statusMap.getOrDefault(id, StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY), "closed"))
                    .collect(Collectors.toList());
            bugSizeMap.put("unClosed", unClosedIds.size());
        }

        int thisWeekCount = 0;
        if (CollectionUtils.isNotEmpty(unClosedIds)) {
            thisWeekCount = extIssuesMapper.getThisWeekIssueCount(unClosedIds, projectId).intValue();
        }
        bugSizeMap.put("thisWeekCount", thisWeekCount);
        // 如果没有严重程度字段
        int newCount = 0;
        int resolvedCount = 0;
        int rejectedCount = 0;
        int unKnownCount = 0;
        for (String unClosedId : unClosedIds) {
            String status = statusMap.getOrDefault(unClosedId, StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY);
            if (StringUtils.equalsIgnoreCase(status, "new")) {
                newCount += 1;
            } else if (StringUtils.equalsIgnoreCase(status, "resolved")) {
                resolvedCount += 1;
            } else if (StringUtils.equalsIgnoreCase(status, "rejected")) {
                rejectedCount += 1;
            } else {
                unKnownCount += 1;
            }
        }

        bugSizeMap.put("newCount", newCount);
        bugSizeMap.put("resolvedCount", resolvedCount);
        bugSizeMap.put("rejectedCount", rejectedCount);
        bugSizeMap.put("unKnownCount", unKnownCount);
        return bugSizeMap;
    }

    private int getAllUnClosedBugSize(String projectId) {
        IssuesRequest req = new IssuesRequest();
        req.setProjectId(projectId);
        List<IssuesDao> issues = extIssuesMapper.getIssues(req);
        if (CollectionUtils.isEmpty(issues)) {
            return 0;
        }
        List<String> ids = issues.stream().map(IssuesDao::getId).collect(Collectors.toList());
        Map<String, String> statusMap = customFieldIssuesService.getIssueStatusMap(ids, projectId);

        return (int) issues.stream()
                .filter(i -> !StringUtils.equals(statusMap.getOrDefault(i.getId(), StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY), "closed"))
                .count();
    }

    private double getPlanPassRage(String planId) {
        TestPlanDTOWithMetric testPlan = new TestPlanDTOWithMetric();
        testPlan.setId(planId);
        testPlanService.calcTestPlanRate(testPlan);
        return testPlan.getPassRate();
    }
}
