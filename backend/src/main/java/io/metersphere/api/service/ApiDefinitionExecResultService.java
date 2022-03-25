package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.RequestResultExpandDTO;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.track.dto.PlanReportCaseDTO;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testcase.TrackCount;
import io.metersphere.track.service.TestCaseReviewApiCaseService;
import io.metersphere.track.service.TestPlanTestCaseService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionExecResultService {
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
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
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void saveApiResult(List<RequestResult> requestResults, ResultDTO dto) {
        LoggerUtil.info("接收到API/CASE执行结果【 " + requestResults.size() + " 】");

        for (RequestResult item : requestResults) {
            if (item.getResponseResult() != null && item.getResponseResult().getResponseTime() <= 0) {
                item.getResponseResult().setResponseTime((item.getEndTime() - item.getStartTime()));
            }
            if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                ApiDefinitionExecResult result = this.editResult(item, dto.getReportId(), dto.getConsole(), dto.getRunMode(), dto.getTestId(), null);
                if (result != null) {
                    // 发送通知
                    sendNotice(result);
                }
            }
        }
    }

    public void batchSaveApiResult(List<ResultDTO> resultDTOS, boolean isSchedule) {
        if (CollectionUtils.isEmpty(resultDTOS)) {
            return;
        }
        LoggerUtil.info("接收到API/CASE执行结果【 " + resultDTOS.size() + " 】");

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionExecResultMapper definitionExecResultMapper = sqlSession.getMapper(ApiDefinitionExecResultMapper.class);
        TestPlanApiCaseMapper planApiCaseMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
        TestCaseReviewApiCaseMapper reviewApiCaseMapper = sqlSession.getMapper(TestCaseReviewApiCaseMapper.class);
        ApiTestCaseMapper batchApiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);

        for (ResultDTO dto : resultDTOS) {
            if (CollectionUtils.isNotEmpty(dto.getRequestResults())) {
                for (RequestResult item : dto.getRequestResults()) {
                    if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                        ApiDefinitionExecResult result = this.editResult(item, dto.getReportId(), dto.getConsole(), dto.getRunMode(), dto.getTestId(),
                                definitionExecResultMapper);
                        // 批量更新关联关系状态
                        batchEditStatus(dto.getRunMode(), result.getStatus(), result.getId(), dto.getTestId(),
                                planApiCaseMapper, reviewApiCaseMapper, batchApiTestCaseMapper
                        );

                        if (result != null && !StringUtils.startsWithAny(dto.getRunMode(), "SCHEDULE")) {
                            // 发送通知
                            sendNotice(result);
                        }
                    }
                }
                if (isSchedule) {
                    // 这个方法得优化大批量跑有问题
                    updateTestCaseStates(dto.getTestId());
                    Map<String, String> apiIdResultMap = new HashMap<>();
                    long errorSize = dto.getRequestResults().stream().filter(requestResult -> requestResult.getError() > 0).count();
                    String status = errorSize > 0 || dto.getRequestResults().isEmpty() ? TestPlanApiExecuteStatus.FAILD.name() : TestPlanApiExecuteStatus.SUCCESS.name();
                    if (StringUtils.isNotEmpty(dto.getReportId())) {
                        apiIdResultMap.put(dto.getReportId(), status);
                    }
                    LoggerUtil.info("TestPlanReportId[" + dto.getTestPlanReportId() + "] API CASE OVER. API CASE STATUS:" + JSONObject.toJSONString(apiIdResultMap));
                }
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
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
            User user = null;
            if (SessionUtils.getUser() != null && StringUtils.equals(SessionUtils.getUser().getId(), result.getUserId())) {
                user = SessionUtils.getUser();
            } else {
                user = userMapper.selectByPrimaryKey(result.getUserId());
            }
            Map paramMap = new HashMap<>(beanMap);
            paramMap.put("operator", user != null ? user.getName() : result.getUserId());
            paramMap.put("status", result.getStatus());
            String context = "${operator}执行接口用例" + status + ": ${name}";
            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(result.getUserId() != null ? result.getUserId() : SessionUtils.getUserId())
                    .context(context)
                    .subject("接口用例通知")
                    .successMailTemplate("api/CaseResultSuccess")
                    .failedMailTemplate("api/CaseResultFailed")
                    .paramMap(paramMap)
                    .event(event)
                    .build();

            String taskType = NoticeConstants.TaskType.API_DEFINITION_TASK;
            Project project = projectMapper.selectByPrimaryKey(apiTestCaseWithBLOBs.getProjectId());
            noticeSendService.send(project, taskType, noticeModel);
        } catch (Exception e) {
            LogUtil.error("消息发送失败：" + e.getMessage());
        }
    }

    public void setExecResult(String id, String status, Long time) {
        TestPlanApiCase apiCase = new TestPlanApiCase();
        apiCase.setId(id);
        apiCase.setStatus(status);
        apiCase.setUpdateTime(time);
        testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
    }

    public void editStatus(ApiDefinitionExecResult saveResult, String type, String status, Long time, String reportId, String testId) {
        String name = testId;
        String version = "";
        String projectId = "";
        if (StringUtils.equalsAnyIgnoreCase(type, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.MANUAL_PLAN.name())) {
            TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(testId);
            ApiTestCaseWithBLOBs caseWithBLOBs = null;
            if (testPlanApiCase != null) {
                this.setExecResult(testId, status, time);
                caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(testPlanApiCase.getApiCaseId());
                testPlanApiCase.setStatus(status);
                testPlanApiCase.setUpdateTime(System.currentTimeMillis());
                testPlanApiCaseMapper.updateByPrimaryKeySelective(testPlanApiCase);
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
                name = caseWithBLOBs.getName();
                version = caseWithBLOBs.getVersionId();
                projectId = caseWithBLOBs.getProjectId();
            }
        } else {
            ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(testId);
            if (apiDefinition != null) {
                name = apiDefinition.getName();
                projectId = apiDefinition.getProjectId();
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
                    name = caseWithBLOBs.getName();
                    version = caseWithBLOBs.getVersionId();
                    projectId = caseWithBLOBs.getProjectId();
                }
            }
        }
        if (StringUtils.isEmpty(saveResult.getProjectId()) && StringUtils.isNotEmpty(projectId)) {
            saveResult.setProjectId(projectId);
        }
        saveResult.setVersionId(version);
        saveResult.setName(name);
    }

    public void batchEditStatus(String type, String status, String reportId, String testId,
                                TestPlanApiCaseMapper batchTestPlanApiCaseMapper,
                                TestCaseReviewApiCaseMapper batchReviewApiCaseMapper,
                                ApiTestCaseMapper batchApiTestCaseMapper) {
        if (StringUtils.equalsAnyIgnoreCase(type, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(),
                ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.MANUAL_PLAN.name())) {
            TestPlanApiCase apiCase = new TestPlanApiCase();
            apiCase.setId(testId);
            apiCase.setStatus(status);
            apiCase.setUpdateTime(System.currentTimeMillis());
            batchTestPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);

            TestCaseReviewApiCase reviewApiCase = new TestCaseReviewApiCase();
            reviewApiCase.setId(testId);
            reviewApiCase.setStatus(status);
            reviewApiCase.setUpdateTime(System.currentTimeMillis());
            batchReviewApiCaseMapper.updateByPrimaryKeySelective(reviewApiCase);
        } else {
            // 更新用例最后执行结果
            ApiTestCaseWithBLOBs caseWithBLOBs = new ApiTestCaseWithBLOBs();
            caseWithBLOBs.setId(testId);
            caseWithBLOBs.setLastResultId(reportId);
            caseWithBLOBs.setStatus(status);
            caseWithBLOBs.setUpdateTime(System.currentTimeMillis());
            batchApiTestCaseMapper.updateByPrimaryKeySelective(caseWithBLOBs);
        }
    }

    /**
     * 定时任务触发的保存逻辑
     * 定时任务时，userID要改为定时任务中的用户
     */
    public void saveApiResultByScheduleTask(List<RequestResult> requestResults, ResultDTO dto) {
        if (CollectionUtils.isNotEmpty(requestResults)) {
            LoggerUtil.info("接收到定时任务执行结果【 " + requestResults.size() + " 】");
            for (RequestResult item : requestResults) {
                if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                    //对响应内容进行进一步解析。如果有附加信息（比如误报库信息），则根据附加信息内的数据进行其他判读
                    RequestResultExpandDTO expandDTO = ResponseUtil.parseByRequestResult(item);

                    ApiDefinitionExecResult reportResult = this.editResult(item, dto.getReportId(), dto.getConsole(), dto.getRunMode(), dto.getTestId(), null);
                    String status = item.isSuccess() ? "success" : "error";
                    if (reportResult != null) {
                        status = reportResult.getStatus();
                    }
                    if (MapUtils.isNotEmpty(expandDTO.getAttachInfoMap())) {
                        status = expandDTO.getStatus();
                    }
                    if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
                        TestPlanApiCase apiCase = testPlanApiCaseMapper.selectByPrimaryKey(dto.getTestId());
                        if (apiCase != null) {
                            apiCase.setStatus(status);
                            apiCase.setUpdateTime(System.currentTimeMillis());
                            testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
                        }
                    } else {
                        this.setExecResult(dto.getTestId(), status, item.getStartTime());
                        testCaseReviewApiCaseService.setExecResult(dto.getTestId(), status, item.getStartTime());
                    }
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
        LoggerUtil.info("TestPlanReportId[" + dto.getTestPlanReportId() + "] APICASE OVER. API CASE STATUS:" + JSONObject.toJSONString(apiIdResultMap));
    }


    /**
     * 更新测试计划中, 关联接口测试的功能用例的状态
     */
    public void updateTestCaseStates(String testPlanApiCaseId) {
        try {
            TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(testPlanApiCaseId);
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
                    List<TestPlanDTO> dtoList = extTestPlanMapper.selectTestPlanByRelevancy(planRequest);
                    item.setTestPlanDTOList(dtoList);
                    returnList.add(item);
                } else {
                    break;
                }
            }

            return returnList;
        }
    }

    private ApiDefinitionExecResult editResult(RequestResult item, String reportId, String console, String type, String testId, ApiDefinitionExecResultMapper batchMapper) {
        if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
            ApiDefinitionExecResult saveResult = new ApiDefinitionExecResult();
            item.getResponseResult().setConsole(console);
            saveResult.setId(reportId);
            //对响应内容进行进一步解析。如果有附加信息（比如误报库信息），则根据附加信息内的数据进行其他判读
            RequestResultExpandDTO expandDTO = ResponseUtil.parseByRequestResult(item);
            String status = item.isSuccess() ? ExecuteResult.success.name() : ExecuteResult.error.name();
            if (MapUtils.isNotEmpty(expandDTO.getAttachInfoMap())) {
                status = expandDTO.getStatus();
                saveResult.setContent(JSON.toJSONString(expandDTO));
            } else {
                saveResult.setContent(JSON.toJSONString(item));
            }
            saveResult.setType(type);

            saveResult.setStatus(status);
            saveResult.setResourceId(item.getName());
            saveResult.setStartTime(item.getStartTime());
            saveResult.setEndTime(item.getEndTime());

            if (StringUtils.isNotEmpty(saveResult.getTriggerMode()) && saveResult.getTriggerMode().equals("CASE")) {
                saveResult.setTriggerMode(TriggerMode.MANUAL.name());
            }
            if (batchMapper == null) {
                editStatus(saveResult, type, status, saveResult.getCreateTime(), saveResult.getId(), testId);
                apiDefinitionExecResultMapper.updateByPrimaryKeySelective(saveResult);
            } else {
                batchMapper.updateByPrimaryKeySelective(saveResult);
            }
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

    public List<ApiDefinitionExecResultExpand> exceReportlist(QueryAPIReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiDefinitionExecResultExpand> list = extApiDefinitionExecResultMapper.list(request);
        List<String> userIds = list.stream().map(ApiDefinitionExecResult::getUserId)
                .collect(Collectors.toList());
        Map<String, User> userMap = ServiceUtils.getUserMap(userIds);
        list.forEach(item -> {
            User user = userMap.get(item.getUserId());
            if (user != null)
                item.setUserName(user.getName());
        });
        return list;
    }
}
