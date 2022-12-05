package io.metersphere.controller;


import io.metersphere.base.domain.TestCase;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.dto.BugStatistics;
import io.metersphere.dto.TrackCountResult;
import io.metersphere.dto.TrackStatisticsDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.plan.dto.ChartsData;
import io.metersphere.service.TestCaseService;
import io.metersphere.service.TrackService;
import io.metersphere.utils.DiscoveryUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.List;

@RestController
@RequestMapping("/track")
public class TrackController {
    @Resource
    private TrackService trackService;
    @Resource
    private TestCaseService testCaseService;

    @GetMapping("/count/{projectId}")
    public TrackStatisticsDTO getTrackCount(@PathVariable String projectId) {
        TrackStatisticsDTO statistics = new TrackStatisticsDTO();

        List<TrackCountResult> priorityResults = trackService.countPriority(projectId);
        statistics.countPriority(priorityResults);

        long size = trackService.countCreatedThisWeek(projectId);
        statistics.setThisWeekAddedCount(size);

        List<TrackCountResult> statusResults = trackService.countStatus(projectId);
        statistics.countStatus(statusResults);

        long total = statistics.getPrepareCount() + statistics.getPassCount() + statistics.getUnPassCount();
        if (total != 0) {
            float reviewed = (float) (statistics.getPassCount() + statistics.getUnPassCount()) * 100 / total;
            DecimalFormat df = new DecimalFormat("0.0");
            statistics.setReviewRage(df.format(reviewed) + "%");
        }

        long reviewedTotal = statistics.getPassCount() + statistics.getUnPassCount();
        if (reviewedTotal != 0) {
            float reviewPass = (float) statistics.getPassCount() * 100 / (statistics.getPassCount() + statistics.getUnPassCount());
            DecimalFormat df = new DecimalFormat("0.0");
            statistics.setReviewPassRage(df.format(reviewPass) + "%");
        }

        return statistics;
    }

    @GetMapping("/relevance/count/{projectId}")
    public TrackStatisticsDTO getRelevanceCount(@PathVariable String projectId) {
        TrackStatisticsDTO statistics = new TrackStatisticsDTO();

        boolean queryUi = DiscoveryUtil.hasService(MicroServiceName.UI_TEST);
        List<TrackCountResult> relevanceResults = trackService.countRelevance(projectId, queryUi);
        statistics.countRelevance(relevanceResults);

        long size = trackService.countRelevanceCreatedThisWeek(projectId);
        statistics.setThisWeekAddedCount(size);

        List<TestCase> list = testCaseService.getTestCaseByProjectId(projectId);
        long total = list.size();
        int coverage = trackService.countCoverage(projectId, queryUi);
        statistics.setCoverageCount(coverage);
        statistics.setUncoverageCount(total - coverage);

        if (total != 0) {
            float coverageRageNumber = (float) statistics.getCoverageCount() * 100 / total;
            DecimalFormat df = new DecimalFormat("0.0");
            statistics.setCoverageRage(df.format(coverageRageNumber) + "%");
        }

        statistics.setApiCaseCountStr(Translator.get("api_case") + "<br/><br/>" + statistics.getApiCaseCount());
        statistics.setPerformanceCaseCountStr(Translator.get("performance_case") + "<br/><br/>" + statistics.getPerformanceCaseCount());
        statistics.setScenarioCaseStr(Translator.get("scenario_case") + "<br/><br/>" + statistics.getScenarioCaseCount());
        if (queryUi) {
            statistics.setUiScenarioCaseStr(Translator.get("ui_scenario_case") + "<br/><br/>" + statistics.getUiScenarioCaseCount());
        }

        return statistics;
    }

    @GetMapping("/case/bar/{projectId}")
    public List<ChartsData> getCaseMaintenanceBar(@PathVariable String projectId) {
        return trackService.getCaseMaintenanceBar(projectId);
    }

    @GetMapping("/bug/count/{projectId}")
    public BugStatistics getBugStatistics(@PathVariable String projectId) {
        return trackService.getBugStatistics(projectId);
    }
}
