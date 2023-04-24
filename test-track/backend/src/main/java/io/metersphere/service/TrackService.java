package io.metersphere.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.CustomField;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.CustomFieldScene;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.IssueStatus;
import io.metersphere.constants.SystemCustomField;
import io.metersphere.dto.BugStatistics;
import io.metersphere.dto.ExecutedCaseInfoResult;
import io.metersphere.dto.TestPlanDTOWithMetric;
import io.metersphere.dto.TrackCountResult;
import io.metersphere.i18n.Translator;
import io.metersphere.plan.dto.ChartsData;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.request.testcase.TrackCount;
import io.metersphere.xpack.track.dto.IssuesDao;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private BaseCustomFieldService baseCustomFieldService;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private ExtIssuesMapper extIssuesMapper;

    public List<TrackCountResult> countPriority(String projectId) {
        List<TrackCountResult> trackCountResults = extTestCaseMapper.countPriority(projectId);
        trackCountResults.forEach(trackCountResult -> {
            String groupField = trackCountResult.getGroupField();
            if (StringUtils.isNotEmpty(groupField) && !StringUtils.equalsAnyIgnoreCase(groupField,
                    TrackCount.P0, TrackCount.P1, TrackCount.P2, TrackCount.P3)) {
                // 系统字段自定义选项值
                CustomField priorityField = baseCustomFieldService.getCustomFieldByName(SessionUtils.getCurrentProjectId(), SystemCustomField.CASE_PRIORITY);
                if (priorityField != null && StringUtils.equals(priorityField.getScene(), CustomFieldScene.TEST_CASE.name())) {
                    String options = priorityField.getOptions();
                    List<Map> optionMapList = JSONArray.parseArray(options, Map.class);
                    optionMapList.forEach(optionMap -> {
                        String text = optionMap.get("text").toString();
                        String value = optionMap.get("value").toString();
                        if (StringUtils.equals(groupField, value)) {
                            trackCountResult.setGroupField(text);
                        }
                    });
                }
            }
        });
        return trackCountResults;
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

    public List<TrackCountResult> countRelevance(String projectId, boolean queryUI) {
        return extTestCaseMapper.countRelevance(projectId, queryUI);
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

    public int countCoverage(String projectId, boolean queryUi) {
        return extTestCaseMapper.countCoverage(projectId, queryUi);
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
        BugStatistics bugStatistics = new BugStatistics();
        Map<String, Integer> bugStatusMap = getPlanBugStatusSize(projectId);
        Map<String, Integer> bugSizeMap = getPlanBugSize(projectId);
        int totalPlanBugSize = bugSizeMap.get("total") == null ? 0 : bugSizeMap.get("total");
        int totalUnClosedPlanBugSize = bugSizeMap.get("unClosed") == null ? 0 : bugSizeMap.get("unClosed");
        int thisWeekCount = bugSizeMap.get("thisWeekCount") == null ? 0 : bugSizeMap.get("thisWeekCount");

        bugStatistics.setBugUnclosedCount(totalUnClosedPlanBugSize);
        bugStatistics.setBugTotalCount(totalPlanBugSize);

        float rage = totalPlanBugSize == 0 ? 0 : (float) totalUnClosedPlanBugSize * 100 / totalPlanBugSize;
        DecimalFormat df = new DecimalFormat("0.0");
        bugStatistics.setUnClosedRage(df.format(rage) + "%");
        bugStatistics.setThisWeekCount(thisWeekCount);
        bugStatistics.setChartData(bugStatusMap);
        return bugStatistics;
    }

    private Map<String, Integer> getPlanBugSize(String projectId) {
        Map<String, Integer> bugSizeMap = new HashMap<>();

        List<String> issueIds = extIssuesMapper.getTestPlanIssue(projectId);
        bugSizeMap.put("total", issueIds.size());
        Map<String, String> statusMap = customFieldIssuesService.getIssueStatusMap(issueIds, projectId);

        if (MapUtils.isEmpty(statusMap) && CollectionUtils.isNotEmpty(issueIds)) {
            // 未找到自定义字段状态, 则获取平台状态
            IssuesRequest issuesRequest = new IssuesRequest();
            issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
            issuesRequest.setFilterIds(issueIds);
            List<IssuesDao> issues = extIssuesMapper.getIssues(issuesRequest);
            statusMap = issues.stream().collect(Collectors.toMap(IssuesDao::getId, i -> Optional.ofNullable(i.getPlatformStatus()).orElse("new")));
        }

        if (MapUtils.isNotEmpty(statusMap)) {
            Map<String, String> tmpStatusMap = statusMap;
            issueIds = issueIds.stream()
                    .filter(id -> !StringUtils.equals(tmpStatusMap.getOrDefault(id, StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY), "closed"))
                    .collect(Collectors.toList());
        }

        bugSizeMap.put("unClosed", issueIds.size());

        int thisWeekCount = 0;
        if (CollectionUtils.isNotEmpty(issueIds)) {
            Map<String, Date> weedFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
            Date firstDay = weedFirstTimeAndLastTime.get("firstTime");
            Date lastDay = weedFirstTimeAndLastTime.get("lastTime");
            thisWeekCount = extTestCaseMapper.getTestPlanThisWeekBugCount(projectId, issueIds, firstDay.getTime(), lastDay.getTime()).intValue();
        }
        bugSizeMap.put("thisWeekCount", thisWeekCount);
        return bugSizeMap;
    }

    private Map<String, Integer> getPlanBugStatusSize(String projectId) {
        CustomField customField = baseCustomFieldService.getCustomFieldByName(projectId, SystemCustomField.ISSUE_STATUS);
        JSONArray statusArray = JSONArray.parseArray(customField.getOptions());
        Map<String, Integer> bugStatusMap = new HashMap<>();

        if (StringUtils.isNotEmpty(projectId)) {
            List<String> issueIds = extIssuesMapper.getTestPlanIssue(projectId);
            Map<String, String> statusMap = customFieldIssuesService.getIssueStatusMap(issueIds, projectId);
            if (MapUtils.isEmpty(statusMap) && CollectionUtils.isNotEmpty(issueIds)) {
                // 未找到自定义字段状态, 则获取平台状态
                IssuesRequest issuesRequest = new IssuesRequest();
                issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
                issuesRequest.setFilterIds(issueIds);
                List<IssuesDao> issues = extIssuesMapper.getIssues(issuesRequest);
                statusMap = issues.stream().collect(Collectors.toMap(IssuesDao::getId, i -> Optional.ofNullable(i.getPlatformStatus()).orElse("new")));
            }

            if (MapUtils.isEmpty(statusMap)) {
                Integer count = bugStatusMap.get(Translator.get("new"));
                if (count == null) {
                    bugStatusMap.put(Translator.get("new"), issueIds.size());
                } else {
                    count += issueIds.size();
                    bugStatusMap.put(Translator.get("new"), count);
                }
            } else {
                Map<String, String> tmpStatusMap = statusMap;
                List<String> unClosedIds = issueIds.stream()
                        .filter(id -> !StringUtils.equals(tmpStatusMap.getOrDefault(id, StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY), "closed"))
                        .collect(Collectors.toList());
                for (String unClosedId : unClosedIds) {
                    String status = statusMap.getOrDefault(unClosedId, StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY);
                    IssueStatus statusEnum = IssueStatus.getEnumByName(status);
                    if (statusEnum != null) {
                        Integer count = bugStatusMap.get(Translator.get(statusEnum.getI18nKey()));
                        if (count == null) {
                            bugStatusMap.put(Translator.get(statusEnum.getI18nKey()), 1);
                        } else {
                            count += 1;
                            bugStatusMap.put(Translator.get(statusEnum.getI18nKey()), count);
                        }
                    } else {
                        boolean isInStatusArray = false;
                        for (Object item : statusArray) {
                            JSONObject statusObj = (JSONObject) item;
                            if (StringUtils.equals(status, statusObj.get("value").toString())) {
                                Integer count = bugStatusMap.get(statusObj.get("text").toString());
                                if (count == null) {
                                    bugStatusMap.put(statusObj.get("text").toString(), 1);
                                } else {
                                    count += 1;
                                    bugStatusMap.put(statusObj.get("text").toString(), count);
                                }
                                isInStatusArray = true;
                            }
                        }

                        if (!isInStatusArray) {
                            Integer count = bugStatusMap.get(status);
                            if (count == null) {
                                bugStatusMap.put(status, 1);
                            } else {
                                count += 1;
                                bugStatusMap.put(status, count);
                            }
                        }
                    }
                }
            }
        }
        return bugStatusMap;
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

    public List<ExecutedCaseInfoResult> findFailureCaseInfoByProjectIDAndLimitNumberInSevenDays(String projectId, String versionId) {

        //获取7天之前的日期
        Date startDay = DateUtils.dateSum(new Date(), -6);
        //将日期转化为 00:00:00 的时间戳
        Date startTime = null;
        try {
            startTime = DateUtils.getDayStartTime(startDay);
        } catch (Exception e) {
            LogUtil.error("解析日期出错!", e);
        }

        if (startTime == null) {
            return new ArrayList<>(0);
        } else {
            int limitNumber = 10;
            List<ExecutedCaseInfoResult> returnList = new ArrayList<>(limitNumber);
            ArrayList<ExecutedCaseInfoResult> allCaseExecList = new ArrayList<>();
            allCaseExecList.addAll(extTestPlanTestCaseMapper.findFailureCaseInTestPlanByProjectIDAndExecuteTimeAndLimitNumber(projectId, versionId, startTime.getTime(), limitNumber));
            allCaseExecList.addAll(extTestPlanApiCaseMapper.findFailureCaseInTestPlanByProjectIDAndExecuteTimeAndLimitNumber(projectId, versionId, startTime.getTime(), limitNumber));
            allCaseExecList.addAll(extTestPlanScenarioCaseMapper.findFailureCaseInTestPlanByProjectIDAndExecuteTimeAndLimitNumber(projectId, versionId, startTime.getTime(), limitNumber));

            if (CollectionUtils.isNotEmpty(allCaseExecList)) {
                allCaseExecList.sort(Comparator.comparing(ExecutedCaseInfoResult::getFailureTimes).reversed());
                for (int i = 0; i < allCaseExecList.size(); i++) {
                    if (i < limitNumber) {
                        ExecutedCaseInfoResult item = allCaseExecList.get(i);
                        returnList.add(item);
                    } else {
                        break;
                    }
                }
            }
            return returnList;
        }
    }
}
