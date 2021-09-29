package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.cache.TestPlanReportExecuteCatch;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.api.jmeter.MessageCache;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestCaseReviewApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestCaseReviewApiCaseService;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanService;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionExecResultService {
    Logger testPlanLog = LoggerFactory.getLogger("testPlanExecuteLog");
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private TestCaseReviewApiCaseService testCaseReviewApiCaseService;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    TestCaseReviewApiCaseMapper testCaseReviewApiCaseMapper;

    @Resource
    private ApiDefinitionService apiDefinitionService;

    @Resource
    private NoticeSendService noticeSendService;

    public ApiDefinitionExecResult getInfo(String id) {
        return apiDefinitionExecResultMapper.selectByPrimaryKey(id);
    }

    public void saveApiResult(TestResult result, String type, String triggerMode) {
        if (CollectionUtils.isNotEmpty(result.getScenarios())) {
            final boolean[] isFirst = {true};
            result.getScenarios().forEach(scenarioResult -> {
                if (scenarioResult != null && CollectionUtils.isNotEmpty(scenarioResult.getRequestResults())) {
                    scenarioResult.getRequestResults().forEach(item -> {
                        ApiDefinitionExecResult saveResult = MessageCache.batchTestCases.get(result.getTestId());
                        if (saveResult == null) {
                            saveResult = apiDefinitionExecResultMapper.selectByPrimaryKey(result.getTestId());
                        }
                        item.getResponseResult().setConsole(result.getConsole());
                        boolean saved = true;
                        if (saveResult == null || scenarioResult.getRequestResults().size() > 1) {
                            saveResult = new ApiDefinitionExecResult();
                            if (isFirst[0]) {
                                isFirst[0] = false;
                                saveResult.setId(result.getTestId());
                            } else {
                                saveResult.setId(UUID.randomUUID().toString());
                            }
                            saveResult.setActuator("LOCAL");
                            saveResult.setName(item.getName());
                            saveResult.setTriggerMode(triggerMode);
                            saveResult.setType(type);
                            saveResult.setCreateTime(item.getStartTime());
                            if (StringUtils.isNotEmpty(result.getUserId())) {
                                saveResult.setUserId(result.getUserId());
                            } else {
                                saveResult.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
                            }
                            saved = false;
                        }
                        String status = item.isSuccess() ? "success" : "error";
                        saveResult.setName(getName(type, item.getName(), status, saveResult.getCreateTime(), saveResult.getId()));
                        saveResult.setStatus(status);
                        saveResult.setResourceId(item.getName());
                        saveResult.setContent(JSON.toJSONString(item));
                        saveResult.setStartTime(item.getStartTime());
                        saveResult.setEndTime(item.getResponseResult().getResponseTime());

                        // 清空上次执行结果的内容，只保留近五条结果
                        ApiDefinitionExecResult prevResult = extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(item.getName(), type);
                        if (prevResult != null) {
                            prevResult.setContent(null);
                            apiDefinitionExecResultMapper.updateByPrimaryKeyWithBLOBs(prevResult);
                        }

                        if (StringUtils.isNotEmpty(saveResult.getTriggerMode()) && saveResult.getTriggerMode().equals("CASE")) {
                            saveResult.setTriggerMode(TriggerMode.MANUAL.name());
                        }
                        if (!saved) {
                            apiDefinitionExecResultMapper.insert(saveResult);
                        } else {
                            apiDefinitionExecResultMapper.updateByPrimaryKeyWithBLOBs(saveResult);
                        }
                        apiDefinitionService.removeCache(result.getTestId());
                        // 发送通知
                        sendNotice(saveResult);
                    });
                }
            });
        }
    }

    private void sendNotice(ApiDefinitionExecResult result) {
        String resourceId = result.getResourceId();
        ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(resourceId);
        // 接口定义直接执行不发通知
        if (apiTestCaseWithBLOBs == null) {
            return;
        }

        BeanMap beanMap = new BeanMap(apiTestCaseWithBLOBs);

        String event;
        String status;
        if (StringUtils.equals(result.getStatus(), "success")) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
            status = "成功";
        } else {
            event = NoticeConstants.Event.EXECUTE_FAILED;
            status = "失败";
        }

        Map paramMap = new HashMap<>(beanMap);
        paramMap.put("operator", SessionUtils.getUser().getName());
        paramMap.put("status", result.getStatus());
        String context = "${operator}执行接口用例" + status + ": ${name}";
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(SessionUtils.getUserId())
                .context(context)
                .subject("接口用例通知")
                .successMailTemplate("api/CaseResult")
                .failedMailTemplate("api/CaseResult")
                .paramMap(paramMap)
                .event(event)
                .build();

        String taskType = NoticeConstants.TaskType.API_DEFINITION_TASK;
        if (StringUtils.equals(ReportTriggerMode.API.name(), result.getTriggerMode())) {
            noticeSendService.send(ReportTriggerMode.API.name(), taskType, noticeModel);
        } else {
            noticeSendService.send(taskType, noticeModel);
        }
    }

    private String getName(String type, String id, String status, Long time, String resourceId) {
        if (id.indexOf(DelimiterConstants.SEPARATOR.toString()) != -1) {
            return id.substring(0, id.indexOf(DelimiterConstants.SEPARATOR.toString()));
        }
        if (StringUtils.equalsAnyIgnoreCase(type, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
            TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(id);
            ApiTestCaseWithBLOBs caseWithBLOBs = null;
            if (testPlanApiCase != null) {
                testPlanApiCaseService.setExecResult(id, status, time);
                caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(testPlanApiCase.getApiCaseId());
                testPlanApiCase.setStatus(status);
                testPlanApiCase.setUpdateTime(System.currentTimeMillis());
                testPlanApiCaseService.updateByPrimaryKeySelective(testPlanApiCase);
            }
            TestCaseReviewApiCase testCaseReviewApiCase = testCaseReviewApiCaseMapper.selectByPrimaryKey(id);
            if (testCaseReviewApiCase != null) {
                testCaseReviewApiCaseService.setExecResult(id, status, time);
                caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(testCaseReviewApiCase.getApiCaseId());
                testCaseReviewApiCase.setStatus(status);
                testCaseReviewApiCase.setUpdateTime(System.currentTimeMillis());
                testCaseReviewApiCaseService.updateByPrimaryKeySelective(testCaseReviewApiCase);
            }
            if (caseWithBLOBs != null) {
                return caseWithBLOBs.getName();
            }
        } else {
            ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
            if (apiDefinition != null) {
                return apiDefinition.getName();
            } else {
                ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(id);
                if (caseWithBLOBs != null) {
                    // 更新用例最后执行结果
                    caseWithBLOBs.setLastResultId(resourceId);
                    caseWithBLOBs.setStatus(status);
                    caseWithBLOBs.setUpdateTime(System.currentTimeMillis());
                    apiTestCaseMapper.updateByPrimaryKey(caseWithBLOBs);
                    return caseWithBLOBs.getName();
                }
            }
        }
        return id;
    }

    /**
     * 定时任务触发的保存逻辑
     * 定时任务时，userID要改为定时任务中的用户
     *
     * @param result
     * @param type
     */
    public void saveApiResultByScheduleTask(TestResult result, String testPlanReportId, String type) {
        testPlanLog.info("TestPlanReportId[" + testPlanReportId + "] APICASE OVER.");
        String saveResultType = type;
        if (StringUtils.equalsAny(saveResultType, ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.MANUAL_PLAN.name())) {
            saveResultType = ApiRunMode.API_PLAN.name();
        }
        String finalSaveResultType = saveResultType;

        Map<String, String> apiIdResultMap = new HashMap<>();
        Map<String, ApiDefinitionExecResult> caseReportMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(result.getScenarios())) {
            result.getScenarios().forEach(scenarioResult -> {
                if (scenarioResult != null && CollectionUtils.isNotEmpty(scenarioResult.getRequestResults())) {
                    scenarioResult.getRequestResults().forEach(item -> {

                        String creator = TestPlanReportExecuteCatch.getCreator(testPlanReportId);

                        String status = item.isSuccess() ? "success" : "error";
                        ApiDefinitionExecResult saveResult = new ApiDefinitionExecResult();
                        saveResult.setId(UUID.randomUUID().toString());
                        saveResult.setCreateTime(System.currentTimeMillis());
                        saveResult.setName(getName(type, item.getName(), status, saveResult.getCreateTime(), saveResult.getId()));
                        ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionMapper.selectByPrimaryKey(item.getName());
                        String caseId = null;
                        if (apiDefinitionWithBLOBs != null) {
                            saveResult.setName(apiDefinitionWithBLOBs.getName());
                            caseId = apiDefinitionWithBLOBs.getId();
                        } else {
                            ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(item.getName());
                            if (caseWithBLOBs != null) {
                                caseId = caseWithBLOBs.getId();
                                saveResult.setName(caseWithBLOBs.getName());
                            } else {
                                caseWithBLOBs = testPlanApiCaseService.getApiTestCaseById(item.getName());
                                if (caseWithBLOBs != null) {
                                    saveResult.setName(caseWithBLOBs.getName());
                                    caseId = caseWithBLOBs.getId();
                                }
                            }
                        }
                        if (StringUtils.isNotEmpty(caseId)) {
                            apiIdResultMap.put(caseId, item.isSuccess() ? TestPlanApiExecuteStatus.SUCCESS.name() : TestPlanApiExecuteStatus.FAILD.name());
                        }

                        if (StringUtils.equals(type, ApiRunMode.JENKINS_API_PLAN.name())) {
                            saveResult.setTriggerMode(TriggerMode.API.name());
                        } else if (StringUtils.equals(type, ApiRunMode.MANUAL_PLAN.name())) {
                            saveResult.setTriggerMode(TriggerMode.MANUAL.name());
                        } else {
                            saveResult.setTriggerMode(TriggerMode.SCHEDULE.name());
                        }

                        saveResult.setResourceId(item.getName());
                        saveResult.setActuator("LOCAL");
                        saveResult.setContent(JSON.toJSONString(item));
                        saveResult.setStartTime(item.getStartTime());
                        saveResult.setEndTime(item.getResponseResult().getResponseTime());
                        saveResult.setType(finalSaveResultType);
                        saveResult.setStatus(status);

                        if (StringUtils.equals(type, ApiRunMode.SCHEDULE_API_PLAN.name())) {
                            TestPlanApiCase apiCase = testPlanApiCaseService.getById(item.getName());
                            if (StringUtils.isEmpty(creator)) {
                                creator = testPlanService.findScheduleCreateUserById(apiCase.getTestPlanId());
                            }
                            apiCase.setStatus(status);
                            apiCase.setUpdateTime(System.currentTimeMillis());
                            testPlanApiCaseService.updateByPrimaryKeySelective(apiCase);
                        } else if (StringUtils.equals(type, ApiRunMode.JENKINS_API_PLAN.name())) {
                            TestPlanApiCase apiCase = testPlanApiCaseService.getById(item.getName());
                            apiCase.setStatus(status);
                            apiCase.setUpdateTime(System.currentTimeMillis());
                            testPlanApiCaseService.updateByPrimaryKeySelective(apiCase);
                        } else {
                            testPlanApiCaseService.setExecResult(item.getName(), status, item.getStartTime());
                            testCaseReviewApiCaseService.setExecResult(item.getName(), status, item.getStartTime());
                        }
                        if (creator == null) {
                            creator = "";
                        }
                        saveResult.setUserId(creator);
                        // 前一条数据内容清空
                        ApiDefinitionExecResult prevResult = extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(item.getName(), finalSaveResultType);
                        if (prevResult != null) {
                            prevResult.setContent(null);
                            apiDefinitionExecResultMapper.updateByPrimaryKeyWithBLOBs(prevResult);
                        }
                        apiDefinitionExecResultMapper.insert(saveResult);
                        caseReportMap.put(caseId, saveResult);
                    });
                }
            });
        }
        testPlanLog.info("TestPlanReportId[" + testPlanReportId + "] APICASE OVER. API CASE STATUS:" + JSONObject.toJSONString(apiIdResultMap));
        TestPlanReportExecuteCatch.updateApiTestPlanExecuteInfo(testPlanReportId, apiIdResultMap, null, null);
        TestPlanReportExecuteCatch.updateTestPlanExecuteResultInfo(testPlanReportId, caseReportMap, null, null);
    }

    public void deleteByResourceId(String resourceId) {
        ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
        example.createCriteria().andResourceIdEqualTo(resourceId);
        apiDefinitionExecResultMapper.deleteByExample(example);
    }

    public void deleteByResourceIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
        example.createCriteria().andResourceIdIn(ids);
        apiDefinitionExecResultMapper.deleteByExample(example);
    }

    public long countByTestCaseIDInProjectAndExecutedInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiDefinitionExecResultMapper.countByProjectIDAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public long countByTestCaseIDInProject(String projectId) {
        return extApiDefinitionExecResultMapper.countByTestCaseIDInProject(projectId);

    }

    public List<ExecutedCaseInfoResult> findFaliureCaseInfoByProjectIDAndLimitNumberInSevenDays(String projectId, int limitNumber) {

        //获取7天之前的日期
        Date startDay = DateUtils.dateSum(new Date(), -6);
        //将日期转化为 00:00:00 的时间戳
        Date startTime = null;
        try {
            startTime = DateUtils.getDayStartTime(startDay);
        } catch (Exception e) {
        }

        if (startTime == null) {
            return new ArrayList<>(0);
        } else {
            List<ExecutedCaseInfoResult> list = extApiDefinitionExecResultMapper.findFaliureCaseInfoByProjectIDAndExecuteTimeAndLimitNumber(projectId, startTime.getTime());

            List<ExecutedCaseInfoResult> returnList = new ArrayList<>(limitNumber);

            for (int i = 0; i < list.size(); i++) {
                if (i < limitNumber) {
                    //开始遍历查询TestPlan信息 --> 提供前台做超链接
                    ExecutedCaseInfoResult item = list.get(i);

                    QueryTestPlanRequest planRequest = new QueryTestPlanRequest();
                    planRequest.setProjectId(projectId);
                    if ("scenario".equals(item.getCaseType())) {
                        planRequest.setScenarioId(item.getTestCaseID());
                    } else if ("apiCase".equals(item.getCaseType())) {
                        planRequest.setApiId(item.getTestCaseID());
                    } else if ("load".equals(item.getCaseType())) {
                        planRequest.setLoadId(item.getTestCaseID());
                    }
                    List<TestPlanDTO> dtoList = testPlanService.selectTestPlanByRelevancy(planRequest);
                    item.setTestPlanDTOList(dtoList);
                    returnList.add(item);
                } else {
                    break;
                }
            }

            return returnList;
        }
    }
}
