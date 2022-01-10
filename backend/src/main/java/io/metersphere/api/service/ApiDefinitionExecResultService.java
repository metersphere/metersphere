package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestCaseReviewApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.track.dto.PlanReportCaseDTO;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testcase.TrackCount;
import io.metersphere.track.service.TestCaseReviewApiCaseService;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanService;
import io.metersphere.track.service.TestPlanTestCaseService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    private TestCaseReviewApiCaseMapper testCaseReviewApiCaseMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    private ApiTestCaseService apiTestCaseService;

    public void saveApiResult(List<RequestResult> requestResults, ResultDTO dto) {
        boolean isFirst = true;
        int count = requestResults.stream()
                .filter(item -> !StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_"))
                .collect(Collectors.toList()).size();

        LoggerUtil.info("接收到API/CASE执行结果【 " + requestResults.size() + " 】");

        for (RequestResult item : requestResults) {
            item.setEndTime(System.currentTimeMillis());
            if (item.getResponseResult() != null) {
                item.getResponseResult().setResponseTime((item.getEndTime() - item.getStartTime()));
            }
            if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                ApiDefinitionExecResult result = this.save(item, dto.getReportId(), dto.getConsole(), count, dto.getRunMode(), dto.getTestId(), isFirst);
                if (result != null) {
                    // 发送通知
                    sendNotice(result);
                }
                isFirst = false;
            }
        }
    }

    private void sendNotice(ApiDefinitionExecResult result) {
        try {
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
                    .successMailTemplate("api/CaseResultSuccess")
                    .failedMailTemplate("api/CaseResultFailed")
                    .paramMap(paramMap)
                    .event(event)
                    .build();

            String taskType = NoticeConstants.TaskType.API_DEFINITION_TASK;
            if (StringUtils.equals(ReportTriggerMode.API.name(), result.getTriggerMode())) {
                noticeSendService.send(ReportTriggerMode.API.name(), taskType, noticeModel);
            } else {
                noticeSendService.send(taskType, noticeModel);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private String editStatus(String type, String status, Long time, String reportId, String testId) {
        if (StringUtils.equalsAnyIgnoreCase(type, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.MANUAL_PLAN.name())) {
            TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(testId);
            ApiTestCaseWithBLOBs caseWithBLOBs = null;
            if (testPlanApiCase != null) {
                testPlanApiCaseService.setExecResult(testId, status, time);
                caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(testPlanApiCase.getApiCaseId());
                testPlanApiCase.setStatus(status);
                testPlanApiCase.setUpdateTime(System.currentTimeMillis());
                testPlanApiCaseService.updateByPrimaryKeySelective(testPlanApiCase);
                if (LoggerUtil.getLogger().isDebugEnabled()) {
                    LoggerUtil.debug("更新测试计划用例【 " + testPlanApiCase.getId() + " 】");
                }
            }
            TestCaseReviewApiCase testCaseReviewApiCase = testCaseReviewApiCaseMapper.selectByPrimaryKey(testId);
            if (testCaseReviewApiCase != null) {
                testCaseReviewApiCaseService.setExecResult(testId, status, time);
                caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(testCaseReviewApiCase.getApiCaseId());
                testCaseReviewApiCase.setStatus(status);
                testCaseReviewApiCase.setUpdateTime(System.currentTimeMillis());
                testCaseReviewApiCaseService.updateByPrimaryKeySelective(testCaseReviewApiCase);

                if (LoggerUtil.getLogger().isDebugEnabled()) {
                    LoggerUtil.debug("更新用例评审用例【 " + testCaseReviewApiCase.getId() + " 】");
                }
            }
            if (caseWithBLOBs != null) {
                return caseWithBLOBs.getName();
            }
        } else {
            ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(testId);
            if (apiDefinition != null) {
                return apiDefinition.getName();
            } else {
                ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(testId);
                if (caseWithBLOBs != null) {
                    // 更新用例最后执行结果
                    caseWithBLOBs.setLastResultId(reportId);
                    caseWithBLOBs.setStatus(status);
                    caseWithBLOBs.setUpdateTime(System.currentTimeMillis());
                    apiTestCaseMapper.updateByPrimaryKey(caseWithBLOBs);

                    if (LoggerUtil.getLogger().isDebugEnabled()) {
                        LoggerUtil.debug("更新用例【 " + caseWithBLOBs.getId() + " 】");
                    }
                    return caseWithBLOBs.getName();
                }
            }
        }
        return testId;
    }

    /**
     * 定时任务触发的保存逻辑
     * 定时任务时，userID要改为定时任务中的用户
     */
    public void saveApiResultByScheduleTask(List<RequestResult> requestResults, ResultDTO dto) {
        boolean isFirst = true;
        int countExpectProcessResultCount = 0;
        if (CollectionUtils.isNotEmpty(requestResults)) {
            for (RequestResult resultItem : requestResults) {
                if (!StringUtils.startsWithAny(resultItem.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                    countExpectProcessResultCount++;
                }
            }
            LoggerUtil.info("接收到定时任务执行结果【 " + requestResults.size() + " 】");

            for (RequestResult item : requestResults) {
                if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                    this.save(item, dto.getReportId(), dto.getConsole(), countExpectProcessResultCount, dto.getRunMode(), dto.getTestId(), isFirst);
                    String status = item.isSuccess() ? "success" : "error";
                    if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
                        TestPlanApiCase apiCase = testPlanApiCaseService.getById(dto.getTestId());
                        if (apiCase != null) {
                            apiCase.setStatus(status);
                            apiCase.setUpdateTime(System.currentTimeMillis());
                            testPlanApiCaseService.updateByPrimaryKeySelective(apiCase);
                        }
                    } else {
                        testPlanApiCaseService.setExecResult(dto.getTestId(), status, item.getStartTime());
                        testCaseReviewApiCaseService.setExecResult(dto.getTestId(), status, item.getStartTime());
                    }
                    isFirst = false;
                }
            }
        }
        updateTestCaseStates(dto.getTestId());
        Map<String, String> apiIdResultMap = new HashMap<>();
        long errorSize = requestResults.stream().filter(requestResult -> requestResult.getError() > 0).count();
        String status = errorSize > 0 || requestResults.isEmpty() ? TestPlanApiExecuteStatus.FAILD.name() : TestPlanApiExecuteStatus.SUCCESS.name();
        if (StringUtils.isNotEmpty(dto.getReportId())) {
            apiIdResultMap.put(dto.getReportId(), status);
        }
        testPlanLog.info("TestPlanReportId[" + dto.getTestPlanReportId() + "] APICASE OVER. API CASE STATUS:" + JSONObject.toJSONString(apiIdResultMap));
    }

    /**
     * 更新测试计划中, 关联接口测试的功能用例的状态
     */
    public void updateTestCaseStates(String testPlanApiCaseId) {
        try {
            TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(testPlanApiCaseId);
            if (testPlanApiCase == null) return;
            ApiTestCaseWithBLOBs apiTestCase = apiTestCaseService.get(testPlanApiCase.getApiCaseId());
            testPlanTestCaseService.updateTestCaseStates(apiTestCase.getId(), apiTestCase.getName(), testPlanApiCase.getTestPlanId(), TrackCount.TESTCASE);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
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
        Date firstTime = DateUtils.getWeedFirstTimeAndLastTime(new Date()).get("firstTime");
        Date lastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date()).get("lastTime");
        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiDefinitionExecResultMapper.countByProjectIDAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public long countByTestCaseIDInProject(String projectId) {
        return extApiDefinitionExecResultMapper.countByTestCaseIDInProject(projectId);

    }

    public List<ExecutedCaseInfoResult> findFailureCaseInfoByProjectIDAndLimitNumberInSevenDays(String projectId, int limitNumber) {

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

    private ApiDefinitionExecResult save(RequestResult item, String reportId, String console, int expectProcessResultCount, String type, String testId, boolean isFirst) {
        if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
            ApiDefinitionExecResult saveResult = apiDefinitionExecResultMapper.selectByPrimaryKey(reportId);
            if (saveResult == null) {
                saveResult = new ApiDefinitionExecResult();
            }
            item.getResponseResult().setConsole(console);
            saveResult.setId(reportId);
            if (StringUtils.isEmpty(saveResult.getActuator())) {
                saveResult.setActuator("LOCAL");
            }
            saveResult.setName(item.getName());
            saveResult.setType(type);
            saveResult.setCreateTime(item.getStartTime());
            String status = item.isSuccess() ? ExecuteResult.success.name() : ExecuteResult.error.name();
            saveResult.setName(editStatus(type, status, saveResult.getCreateTime(), saveResult.getId(), testId));
            saveResult.setStatus(status);
            saveResult.setResourceId(item.getName());
            saveResult.setContent(JSON.toJSONString(item));
            saveResult.setStartTime(item.getStartTime());
            saveResult.setEndTime(item.getResponseResult().getResponseTime());
            // 清空上次执行结果的内容，只保留近五条结果
            ApiDefinitionExecResult prevResult = extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(item.getName(), type);
            if (prevResult != null) {
                prevResult.setContent(null);
                apiDefinitionExecResultMapper.updateByPrimaryKeySelective(prevResult);
            }

            if (StringUtils.isNotEmpty(saveResult.getTriggerMode()) && saveResult.getTriggerMode().equals("CASE")) {
                saveResult.setTriggerMode(TriggerMode.MANUAL.name());
            }
            apiDefinitionExecResultMapper.updateByPrimaryKeySelective(saveResult);
            return saveResult;
        }
        return null;
    }

    public Map<String, String> selectReportResultByReportIds(Collection<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return new HashMap<>();
        } else {
            Map<String, String> returnMap = new HashMap<>();
            List<ApiDefinitionExecResult> idStatusList = extApiDefinitionExecResultMapper.selectStatusByIdList(values);
            for (ApiDefinitionExecResult model : idStatusList) {
                String id = model.getId();
                String status = model.getStatus();
                returnMap.put(id, status);
            }
            return returnMap;
        }
    }

    public ApiDefinitionExecResult getInfo(String id) {
        return apiDefinitionExecResultMapper.selectByPrimaryKey(id);
    }

    public List<PlanReportCaseDTO> selectForPlanReport(List<String> apiReportIds) {
        if (CollectionUtils.isEmpty(apiReportIds))
            return new ArrayList<>();
        return extApiDefinitionExecResultMapper.selectForPlanReport(apiReportIds);
    }
}
