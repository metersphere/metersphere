package io.metersphere.workstation.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.request.ApiSyncCaseRequest;
import io.metersphere.request.ProjectRequest;
import io.metersphere.request.api.ApiScenarioRequest;
import io.metersphere.request.track.QueryTestCaseRequest;
import io.metersphere.request.track.QueryTestPlanRequest;
import io.metersphere.request.api.ApiTestCaseRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.metersphere.workstation.util.ShareUtil.getTimeMills;

@Service
@Transactional(rollbackFor = Exception.class)
public class WorkstationService {

    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtTestCaseMapper extTestCaseMapper;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;

    @Resource
    private ExtIssuesMapper extIssuesMapper;

    @Resource
    private ExtTestCaseReviewMapper extTestCaseReviewMapper;

    @Resource
    private ExtTestPlanMapper extTestPlanMapper;

    @Resource
    private ExtProjectMapper extProjectMapper;

    @Resource
    private BaseProjectMapper baseProjectMapper;

    @Resource
    private ExtProjectApplicationMapper extProjectApplicationMapper;

    private static final String DEFAULT_TIME_DATE = "-3D";

    public Map<String, Integer> getMyCreatedCaseGroupContMap(Boolean isWeek) {
        long createTime = 0L;
        if (isWeek){
            Date startDayOfWeek = getStartDayOfWeek();
            createTime = startDayOfWeek.getTime();
        }
        String userId = SessionUtils.getUserId();
        //build query condition object
        QueryTestPlanRequest testPlanRequest = new QueryTestPlanRequest();
        testPlanRequest.setUserId(userId);
        testPlanRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        if (isWeek) {
            testPlanRequest.setCreateTime(createTime);
        }
        ApiTestCaseRequest apiTestCaseRequest = new ApiTestCaseRequest();
        apiTestCaseRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        if (isWeek) {
            apiTestCaseRequest.setCreateTime(createTime);
        }
        //@see io/metersphere/base/mapper/ext/ExtApiTestCaseMapper.xml:103
        Map<String, Object> combine = new HashMap<>(2);
        Map<String, String> operatorValue = new HashMap<>(2);
        operatorValue.put("operator", "current user");
        operatorValue.put("value", "current user");
        combine.put("creator", operatorValue);
        testPlanRequest.setCombine(combine);
        apiTestCaseRequest.setCombine(combine);
        ApiScenarioRequest apiScenarioRequest = new ApiScenarioRequest();
        apiScenarioRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        apiScenarioRequest.setCombine(combine);
        if (isWeek) {
            apiScenarioRequest.setCreateTime(createTime);
        }
        QueryTestCaseRequest testCaseRequest = new QueryTestCaseRequest();
        testCaseRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        testCaseRequest.setCombine(combine);
        if (isWeek) {
            testCaseRequest.setCreateTime(createTime);
        }
        //query db
        int apiScenarioCaseCount = extApiScenarioMapper.listModule(apiScenarioRequest);
        int apiTestCaseCount = extApiTestCaseMapper.moduleCount(apiTestCaseRequest);
        int testCaseCount = extTestCaseMapper.moduleCount(testCaseRequest);
        int loadTestCount = extLoadTestMapper.moduleCount(testPlanRequest);
        //build result
        Map<String, Integer> map = new HashMap<>(4);
        map.put("apiScenarioCaseCount", apiScenarioCaseCount);
        map.put("apiTestCaseCount", apiTestCaseCount);
        map.put("testCaseCount", testCaseCount);
        map.put("loadTestCount", loadTestCount);
        return map;

    }

    public Map<String, Integer> getFollowTotalCount(String workstationId){
        String userId = SessionUtils.getUserId();
        List<String> projectIds = extProjectMapper.getProjectIdByWorkspaceId(workstationId);
        if (CollectionUtils.isEmpty(projectIds)) {
            return null;
        }
        int caseFollowCount = extTestCaseMapper.getCountFollow(projectIds, userId);
        int planFollowCount = extTestPlanMapper.getCountFollow(projectIds, userId);
        int reviewFollowCount = extTestCaseReviewMapper.getCountFollow(projectIds, userId);
        int issueFollowCount = extIssuesMapper.getCountFollow(projectIds, userId);
        int apiFollowCount = extApiDefinitionMapper.getCountFollow(projectIds, userId);
        int apiCaseFollowCount = extApiTestCaseMapper.getCountFollow(projectIds, userId);
        int scenarioFollowCount = extApiScenarioMapper.getCountFollow(projectIds, userId);
        int loadFollowCount = extLoadTestMapper.getCountFollow(projectIds, userId);
        Map<String, Integer> map = new HashMap<>(8);
        map.put("track_case", caseFollowCount);
        map.put("track_plan", planFollowCount);
        map.put("track_review", reviewFollowCount);
        map.put("track_issue", issueFollowCount);
        map.put("api_definition", apiFollowCount);
        map.put("api_case", apiCaseFollowCount);
        map.put("api_automation", scenarioFollowCount);
        map.put("performance", loadFollowCount);
        return map;
    }

    public Map<String, Integer> getUpcomingTotalCount(String workstationId){
        String userId = SessionUtils.getUserId();
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setWorkspaceId(workstationId);
        projectRequest.setUserId(userId);
        List<Project> projects = baseProjectMapper.getUserProject(projectRequest);
        if (CollectionUtils.isEmpty(projects)) {
            return null;
        }
        List<String> projectIds = projects.stream().map(Project::getId).toList();
        int caseUpcomingCount = extTestCaseMapper.getCountUpcoming(projectIds, userId);
        int planUpcomingCount = extTestPlanMapper.getCountUpcoming(projectIds, userId);
        int reviewUpcomingCount = extTestCaseReviewMapper.getCountUpcoming(projectIds, userId);
        int issueUpcomingCount = extIssuesMapper.getCountUpcoming(projectIds, userId);
        int apiUpcomingCount = extApiDefinitionMapper.getCountUpcoming(projectIds, userId);
        int apiCaseUpcomingCount = extApiTestCaseMapper.getCountUpcoming(projectIds, userId);
        int scenarioUpcomingCount = extApiScenarioMapper.getCountUpcoming(projectIds, userId);
        int loadUpcomingCount = extLoadTestMapper.getCountUpcoming(projectIds, userId);
        Map<String, Integer> map = new HashMap<>(8);
        map.put("track_case", caseUpcomingCount);
        map.put("track_plan", planUpcomingCount);
        map.put("track_review", reviewUpcomingCount);
        map.put("track_issue", issueUpcomingCount);
        map.put("api_definition", apiUpcomingCount);
        map.put("api_case",apiCaseUpcomingCount);
        map.put("api_automation", scenarioUpcomingCount);
        map.put("performance", loadUpcomingCount);
        return map;
    }

    public List<String> getSyncRuleCaseStatus(String projectId) {
        List<String> statusList = new ArrayList<>();
        List<ProjectApplication> projectApplicationConfigs = extProjectApplicationMapper.selectByProjectIdAndType(projectId,"TRIGGER_UPDATE");
        if (CollectionUtils.isEmpty(projectApplicationConfigs)) {
            statusList.add(ExecuteResult.API_ERROR.getValue());
            return statusList;
        }
        ProjectApplication projectApplication = projectApplicationConfigs.get(0);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            ApiSyncCaseRequest apiSyncCaseRequest = mapper.readValue(projectApplication.getTypeValue(), ApiSyncCaseRequest.class);
            if (apiSyncCaseRequest.getRunError()) {
                statusList.add(ExecuteResult.API_ERROR.getValue());
            }
            if (apiSyncCaseRequest.getUnRun()) {
                statusList.add(ExecuteResult.UN_EXECUTE.getValue());
                statusList.add("");
            }
            return statusList;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return statusList;
    }

    public Long getToBeUpdatedTime(String projectId) {
        if (StringUtils.isBlank(projectId)) {
            return getTimeMills(System.currentTimeMillis(), DEFAULT_TIME_DATE);
        }
        List<ProjectApplication> projectApplications = extProjectApplicationMapper.selectByProjectIdAndType(projectId,"OPEN_UPDATE_TIME");
        if (CollectionUtils.isEmpty(projectApplications)) {
            return getTimeMills(System.currentTimeMillis(), DEFAULT_TIME_DATE);
        }

        List<ProjectApplication> projectApplicationRules = extProjectApplicationMapper.selectByProjectIdAndType(projectId,"TRIGGER_UPDATE");
        ProjectApplication projectApplication = projectApplications.get(0);
        String typeValue = projectApplication.getTypeValue();
        if (CollectionUtils.isEmpty(projectApplicationRules) && StringUtils.equals(typeValue, "false")) {
            return getTimeMills(System.currentTimeMillis(), DEFAULT_TIME_DATE);
        } else if (StringUtils.equals(typeValue, "false")) {
            return null;
        }

        List<ProjectApplication> projectApplicationTimes = extProjectApplicationMapper.selectByProjectIdAndType(projectId,"OPEN_UPDATE_RULE_TIME");
        if (CollectionUtils.isEmpty(projectApplications)) {
            return getTimeMills(System.currentTimeMillis(), DEFAULT_TIME_DATE);
        }
        ProjectApplication projectApplicationTime = projectApplicationTimes.get(0);
        String time = projectApplicationTime.getTypeValue();
        if (StringUtils.isNotBlank(time)) {
            time = "-" + time;
            return getTimeMills(System.currentTimeMillis(), time);
        }
        return null;
    }
    public static Date getStartDayOfWeek() {
        LocalDate date = LocalDate.now();
        TemporalField fieldIso = WeekFields.of(DayOfWeek.MONDAY, 1).dayOfWeek();
        LocalDate localDate = LocalDate.from(date);
        localDate = localDate.with(fieldIso, 1);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Integer getIssueWeekCount(String workstationId) {
        String userId = SessionUtils.getUserId();
        List<String> projectIds = extProjectMapper.getProjectIdByWorkspaceId(workstationId);
        if (CollectionUtils.isEmpty(projectIds)) {
            return null;
        }
        Date startDayOfWeek = getStartDayOfWeek();
        Long createTime = startDayOfWeek.getTime();
        return extIssuesMapper.getCountCreat(projectIds, userId, createTime);
    }
}
