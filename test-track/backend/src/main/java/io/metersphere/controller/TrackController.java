package io.metersphere.controller;


import com.github.pagehelper.Page;
import io.metersphere.base.domain.TestCase;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.plan.dto.ChartsData;
import io.metersphere.request.testcase.TrackCount;
import io.metersphere.service.TestCaseService;
import io.metersphere.service.TrackService;
import io.metersphere.utils.DiscoveryUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/track")
public class TrackController {
    @Resource
    private TrackService trackService;
    @Resource
    private TestCaseService testCaseService;

    @GetMapping("/count/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_HOME)
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

    @GetMapping("/failure/case/about/plan/{projectId}/{versionId}/{pageSize}/{goPage}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_HOME)
    public Pager<List<ExecutedCaseInfoDTO>> failureCaseAboutTestPlan(@PathVariable String projectId, @PathVariable String versionId,
                                                                     @PathVariable int pageSize, @PathVariable int goPage) {
        if (StringUtils.equalsIgnoreCase(versionId, "default")) {
            versionId = null;
        }

        List<ExecutedCaseInfoResult> selectDataList = trackService.findFailureCaseInfoByProjectIDAndLimitNumberInSevenDays(projectId, versionId);
        List<ExecutedCaseInfoDTO> returnList = this.getResultList(goPage, pageSize, selectDataList);
        Page<Object> page = new Page<>(goPage, pageSize);
        page.setTotal(selectDataList.size());
        return PageUtils.setPageInfo(page, returnList);
    }

    private List<ExecutedCaseInfoDTO> getResultList(int goPage, int pageSize, List<ExecutedCaseInfoResult> selectDataList) {
        List<ExecutedCaseInfoDTO> returnList = new ArrayList<>(selectDataList.size());
        for (int dataIndex = goPage * pageSize; dataIndex < selectDataList.size(); dataIndex++) {
            if (returnList.size() < pageSize) {
                ExecutedCaseInfoDTO dataDTO = new ExecutedCaseInfoDTO();
                dataDTO.setSortIndex(dataIndex + 1);
                ExecutedCaseInfoResult selectData = selectDataList.get(dataIndex);
                dataDTO.setCaseID(selectData.getTestCaseID());
                dataDTO.setCaseName(selectData.getCaseName());
                dataDTO.setTestPlan(selectData.getTestPlan());
                dataDTO.setFailureTimes(selectData.getFailureTimes());
                dataDTO.setTestPlanId(selectData.getTestPlanId());
                dataDTO.setCaseType(selectData.getCaseType());
                dataDTO.setId(selectData.getId());
                dataDTO.setTestPlanDTOList(selectData.getTestPlanDTOList());
                dataDTO.setProtocol(selectData.getProtocol());
                dataDTO.setProjectId(selectData.getProjectId());
                returnList.add(dataDTO);
            }
        }
        return returnList;
    }


    @GetMapping("/relevance/count/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_HOME)
    public TrackStatisticsDTO getRelevanceCount(@PathVariable String projectId) {
        TrackStatisticsDTO statistics = new TrackStatisticsDTO();

        boolean queryUi = DiscoveryUtil.hasService(MicroServiceName.UI_TEST);
        List<TrackCountResult> relevanceResults = trackService.countRelevance(projectId, queryUi);
        long uiCountNum = 0L;
        if (!queryUi) {
            List<TrackCountResult> uiGroup =
                    relevanceResults.stream().filter(relevanceResult -> StringUtils.equals(relevanceResult.getGroupField(), TrackCount.UI_AUTOMATION)).toList();
            if (CollectionUtils.isNotEmpty(uiGroup)) {
                uiCountNum = uiGroup.get(0).getCountNumber();
            }
        }
        statistics.countRelevance(relevanceResults, queryUi);

        long size = trackService.countRelevanceCreatedThisWeek(projectId);
        statistics.setThisWeekAddedCount(size);

        List<TestCase> list = testCaseService.getTestCaseByProjectId(projectId);
        long total = list.size();
        int coverage = trackService.countCoverage(projectId, queryUi);
        statistics.setCoverageCount(coverage);
        statistics.setUncoverageCount(total - coverage - uiCountNum);

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
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_HOME)
    public List<ChartsData> getCaseMaintenanceBar(@PathVariable String projectId) {
        return trackService.getCaseMaintenanceBar(projectId);
    }

    @GetMapping("/bug/count/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_HOME)
    public BugStatistics getBugStatistics(@PathVariable String projectId) {
        return trackService.getBugStatistics(projectId);
    }
}
