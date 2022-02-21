package io.metersphere.track.service;

import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.MathUtils;
import io.metersphere.performance.base.ChartsData;
import io.metersphere.service.ProjectService;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.response.BugStatustics;
import io.metersphere.track.response.TestPlanBugCount;
import io.metersphere.track.response.TrackCountResult;
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
    private ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;
    @Resource
    private ProjectService projectService;

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
        for (TestPlan plan : plans) {
            int planBugSize = getPlanBugSize(plan.getId());
            // bug为0不记录
            if (planBugSize == 0) {
                continue;
            }

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

        int totalBugSize = projectService.getProjectBugSize(projectId);
        bugStatustics.setList(list);
        float rage =totalCaseSize == 0 ? 0 : (float) totalBugSize * 100 / totalCaseSize;
        DecimalFormat df = new DecimalFormat("0.0");
        bugStatustics.setRage(df.format(rage) + "%");
        bugStatustics.setBugTotalSize(totalBugSize);
        return bugStatustics;
    }

    private int getPlanCaseSize(String planId) {
        return extTestCaseMapper.getTestPlanCase(planId);

    }

    private int getPlanBugSize(String planId) {
        return extTestCaseMapper.getTestPlanBug(planId);
    }

    private double getPlanPassRage(String planId) {
        TestPlanDTOWithMetric testPlan = new TestPlanDTOWithMetric();
        testPlan.setTested(0);
        testPlan.setPassed(0);
        testPlan.setTotal(0);

        List<String> functionalExecResults = extTestPlanTestCaseMapper.getExecResultByPlanId(planId);
        functionalExecResults.forEach(item -> {
            if (!StringUtils.equals(item, TestPlanTestCaseStatus.Prepare.name())
                    && !StringUtils.equals(item, TestPlanTestCaseStatus.Underway.name())) {
                testPlan.setTested(testPlan.getTested() + 1);
                if (StringUtils.equals(item, TestPlanTestCaseStatus.Pass.name())) {
                    testPlan.setPassed(testPlan.getPassed() + 1);
                }
            }
        });

        List<String> apiExecResults = testPlanApiCaseService.getExecResultByPlanId(planId);
        apiExecResults.forEach(item -> {
            if (StringUtils.isNotBlank(item)) {
                testPlan.setTested(testPlan.getTested() + 1);
                if (StringUtils.equals(item, "success")) {
                    testPlan.setPassed(testPlan.getPassed() + 1);
                }
            }
        });

        List<String> scenarioExecResults = testPlanScenarioCaseService.getExecResultByPlanId(planId);
        scenarioExecResults.forEach(item -> {
            if (StringUtils.isNotBlank(item)) {
                testPlan.setTested(testPlan.getTested() + 1);
                if (StringUtils.equals(item, ScenarioStatus.Success.name())) {
                    testPlan.setPassed(testPlan.getPassed() + 1);
                }
            }
        });

        List<String> loadResults = testPlanLoadCaseService.getStatus(planId);
        loadResults.forEach(item -> {
            if (StringUtils.isNotBlank(item)) {
                testPlan.setTested(testPlan.getTested() + 1);
                if (StringUtils.equals(item, "success")) {
                    testPlan.setPassed(testPlan.getPassed() + 1);
                }
            }
        });
        return MathUtils.getPercentWithDecimal(testPlan.getTested() == 0 ? 0 : testPlan.getPassed() * 1.0 / testPlan.getTested());
    }
}
