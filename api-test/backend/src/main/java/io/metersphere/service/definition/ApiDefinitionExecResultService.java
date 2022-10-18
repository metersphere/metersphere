package io.metersphere.service.definition;

import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.RequestResultExpandDTO;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.plan.TestPlanApiCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.TestPlanApiExecuteStatus;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.ServiceUtils;
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
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiExecutionInfoService apiExecutionInfoService;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ApiCaseExecutionInfoService apiCaseExecutionInfoService;

    /**
     * API/CASE 重试结果保留一条
     *
     * @param dto
     */
    private void mergeRetryResults(ResultDTO dto) {
        List<RequestResult> requestResults = new LinkedList<>();
        if (dto.isRetryEnable() && CollectionUtils.isNotEmpty(dto.getRequestResults())) {
            Map<String, List<RequestResult>> resultMap = dto.getRequestResults().stream().collect(Collectors.groupingBy(RequestResult::getResourceId));
            resultMap.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    requestResults.add(v.get(v.size() - 1));
                }
            });
            dto.setRequestResults(requestResults);
        }
    }

    public void saveApiResult(ResultDTO dto) {
        LoggerUtil.info("接收到API/CASE执行结果【 " + dto.getRequestResults().size() + " 】条");
        this.mergeRetryResults(dto);
        for (RequestResult item : dto.getRequestResults()) {
            if (item.getResponseResult() != null && item.getResponseResult().getResponseTime() <= 0) {
                item.getResponseResult().setResponseTime((item.getEndTime() - item.getStartTime()));
            }
            if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                ApiDefinitionExecResult result = this.editResult(item, dto.getReportId(), dto.getConsole(), dto.getRunMode(), dto.getTestId(), null);
                if (result != null) {
                    result.setResourceId(dto.getTestId());
                    apiExecutionInfoService.insertExecutionInfo(result);
                    User user = null;
                    if (MapUtils.isNotEmpty(dto.getExtendedParameters())) {
                        if (dto.getExtendedParameters().containsKey("userId") && dto.getExtendedParameters().containsKey("userName")) {
                            user = new User() {{
                                this.setId(dto.getExtendedParameters().get("userId").toString());
                                this.setName(dto.getExtendedParameters().get("userName").toString());
                            }};
                            result.setUserId(user.getId());
                        } else if (dto.getExtendedParameters().containsKey("userId")) {
                            result.setUserId(dto.getExtendedParameters().get("userId").toString());
                        }
                    }
                    // 发送通知
                    LoggerUtil.info("执行结果【 " + result.getName() + " 】入库存储完成");
                    sendNotice(result, user);
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
        ApiTestCaseMapper batchApiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);

        for (ResultDTO dto : resultDTOS) {
            this.mergeRetryResults(dto);
            if (CollectionUtils.isNotEmpty(dto.getRequestResults())) {
                for (RequestResult item : dto.getRequestResults()) {
                    if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                        ApiDefinitionExecResult result = this.editResult(item, dto.getReportId(), dto.getConsole(), dto.getRunMode(), dto.getTestId(), definitionExecResultMapper);
                        // 批量更新关联关系状态
                        batchEditStatus(dto.getRunMode(), result.getStatus(), result.getId(), dto.getTestId(), batchApiTestCaseMapper);
                        if (result != null && !StringUtils.startsWithAny(dto.getRunMode(), "SCHEDULE")) {
                            User user = null;
                            if (MapUtils.isNotEmpty(dto.getExtendedParameters()) && dto.getExtendedParameters().containsKey("user") && dto.getExtendedParameters().get("user") instanceof User) {
                                user = (User) dto.getExtendedParameters().get("user");
                            }
                            // 发送通知
                            result.setResourceId(dto.getTestId());
                            sendNotice(result, user);
                        }
                    }
                }
                if (isSchedule) {
                    // 这个方法得优化大批量跑有问题
                    Map<String, String> apiIdResultMap = new HashMap<>();
                    long errorSize = dto.getRequestResults().stream().filter(requestResult -> requestResult.getError() > 0).count();
                    String status = errorSize > 0 || dto.getRequestResults().isEmpty() ? TestPlanApiExecuteStatus.FAILD.name() : TestPlanApiExecuteStatus.SUCCESS.name();
                    if (StringUtils.isNotEmpty(dto.getReportId())) {
                        apiIdResultMap.put(dto.getReportId(), status);
                    }
                    LoggerUtil.info("TestPlanReportId[" + dto.getTestPlanReportId() + "] API CASE OVER. API CASE STATUS:" + JSON.toJSONString(apiIdResultMap));
                }
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void sendNotice(ApiDefinitionExecResult result, User user) {
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
            if (StringUtils.equals(result.getStatus(), ApiReportStatus.SUCCESS.name())) {
                event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
                status = "成功";
            } else {
                event = NoticeConstants.Event.EXECUTE_FAILED;
                status = "失败";
            }
            if (user == null) {
                if (SessionUtils.getUser() != null && StringUtils.equals(SessionUtils.getUser().getId(), result.getUserId())) {
                    user = SessionUtils.getUser();
                } else {
                    user = userMapper.selectByPrimaryKey(result.getUserId());
                }
            }

            Map paramMap = new HashMap<>(beanMap);
            paramMap.put("operator", user != null ? user.getName() : result.getUserId());
            paramMap.put("status", result.getStatus());
            String context = "${operator}执行接口用例" + status + ": ${name}";
            NoticeModel noticeModel = NoticeModel.builder().operator(result.getUserId() != null ? result.getUserId() : SessionUtils.getUserId()).context(context).subject("接口用例通知").paramMap(paramMap).event(event).build();

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
        String version = StringUtils.EMPTY;
        String projectId = StringUtils.EMPTY;
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
                    if (status.equals(ApiReportStatus.ERROR.name())) {
                        caseWithBLOBs.setToBeUpdated(true);
                    } else {
                        caseWithBLOBs.setToBeUpdated(false);
                    }
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

    public void batchEditStatus(String type, String status, String reportId, String testId, ApiTestCaseMapper batchApiTestCaseMapper) {
        // 更新用例最后执行结果
        ApiTestCaseWithBLOBs caseWithBLOBs = new ApiTestCaseWithBLOBs();
        caseWithBLOBs.setId(testId);
        caseWithBLOBs.setLastResultId(reportId);
        caseWithBLOBs.setStatus(status);
        caseWithBLOBs.setUpdateTime(System.currentTimeMillis());
        batchApiTestCaseMapper.updateByPrimaryKeySelective(caseWithBLOBs);
    }

    /**
     * 定时任务触发的保存逻辑
     * 定时任务时，userID要改为定时任务中的用户
     */
    public void saveApiResultByScheduleTask(ResultDTO dto) {
        if (CollectionUtils.isNotEmpty(dto.getRequestResults())) {
            LoggerUtil.info("接收到API/CASE执行结果【 " + dto.getRequestResults().size() + " 】条");
            for (RequestResult item : dto.getRequestResults()) {
                LoggerUtil.info("执行结果【 " + item.getName() + " 】入库存储");
                if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                    //对响应内容进行进一步解析和处理。如果有附加信息（比如误报库信息），则根据附加信息内的数据进行其他判读
                    RequestResultExpandDTO expandDTO = ResponseUtil.parseByRequestResult(item);

                    ApiDefinitionExecResult reportResult = this.editResult(item, dto.getReportId(), dto.getConsole(), dto.getRunMode(), dto.getTestId(), null);
                    if (MapUtils.isNotEmpty(dto.getExtendedParameters()) && dto.getExtendedParameters().containsKey("userId")) {
                        reportResult.setUserId(String.valueOf(dto.getExtendedParameters().get("userId")));
                    }
                    String status = item.isSuccess() ? ApiReportStatus.SUCCESS.name() : ApiReportStatus.ERROR.name();
                    String triggerMode = StringUtils.EMPTY;
                    if (reportResult != null) {
                        status = reportResult.getStatus();
                        triggerMode = reportResult.getTriggerMode();
                    }

                    if (StringUtils.isNotEmpty(expandDTO.getStatus())) {
                        status = expandDTO.getStatus();
                    }
                    if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
                        TestPlanApiCase apiCase = testPlanApiCaseMapper.selectByPrimaryKey(dto.getTestId());
                        if (apiCase != null) {
                            apiCaseExecutionInfoService.insertExecutionInfo(apiCase.getId(), status, triggerMode);
                            apiCase.setStatus(status);
                            apiCase.setUpdateTime(System.currentTimeMillis());
                            testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
                        }
                    } else {
                        this.setExecResult(dto.getTestId(), status, item.getStartTime());
                    }
                }
            }
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

    public List<ExecutedCaseInfoResult> findFailureCaseInfoByProjectIDAndLimitNumberInSevenDays(String projectId, boolean selectFuntionCase, int limitNumber) {

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
            List<ExecutedCaseInfoResult> list = extApiDefinitionExecResultMapper.findFaliureCaseInTestPlanByProjectIDAndExecuteTimeAndLimitNumber(projectId, selectFuntionCase, startTime.getTime());

            List<ExecutedCaseInfoResult> returnList = new ArrayList<>(limitNumber);

            for (int i = 0; i < list.size(); i++) {
                if (i < limitNumber) {
                    //开始遍历查询TestPlan信息 --> 提供前台做超链接
                    ExecutedCaseInfoResult item = list.get(i);
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
            ApiDefinitionExecResultWithBLOBs saveResult = new ApiDefinitionExecResultWithBLOBs();
            item.getResponseResult().setConsole(console);
            saveResult.setId(reportId);
            //对响应内容进行进一步解析。如果有附加信息（比如误报库信息），则根据附加信息内的数据进行其他判读
            RequestResultExpandDTO expandDTO = ResponseUtil.parseByRequestResult(item);
            String status = item.isSuccess() ? ApiReportStatus.SUCCESS.name() : ApiReportStatus.ERROR.name();
            if (MapUtils.isNotEmpty(expandDTO.getAttachInfoMap())) {
                if (StringUtils.isNotEmpty(expandDTO.getStatus())) {
                    status = expandDTO.getStatus();
                }
                saveResult.setContent(JSON.toJSONString(expandDTO));
            } else {
                saveResult.setContent(JSON.toJSONString(item));
            }
            saveResult.setType(type);
            saveResult.setStatus(status);
            saveResult.setStartTime(item.getStartTime());
            saveResult.setEndTime(item.getEndTime());
            if (item.getStartTime() >= item.getEndTime()) {
                saveResult.setEndTime(System.currentTimeMillis());
            }
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


    public List<ApiDefinitionExecResultExpand> apiReportList(QueryAPIReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders(), "end_time"));
        this.initReportRequest(request);
        List<ApiDefinitionExecResultExpand> list = extApiDefinitionExecResultMapper.list(request);
        List<String> userIds = list.stream().map(ApiDefinitionExecResult::getUserId).collect(Collectors.toList());
        Map<String, User> userMap = ServiceUtils.getUserMap(userIds);
        list.forEach(item -> {
            User user = userMap.get(item.getUserId());
            if (user != null) item.setUserName(user.getName());
        });
        return list;
    }

    public List<ApiDefinitionExecResultWithBLOBs> selectByResourceIdsAndMaxCreateTime(List<String> resourceIds) {
        if (CollectionUtils.isNotEmpty(resourceIds)) {
            return extApiDefinitionExecResultMapper.selectByResourceIdsAndMaxCreateTime(resourceIds);
        } else {
            return new ArrayList<>();
        }
    }

    public void deleteByRelevanceTestPlanReportIds(List<String> testPlanReportIdList) {
        if (CollectionUtils.isNotEmpty(testPlanReportIdList)) {
            ApiDefinitionExecResultExample apiDefinitionExecResultExample = new ApiDefinitionExecResultExample();
            apiDefinitionExecResultExample.createCriteria().andRelevanceTestPlanReportIdIn(testPlanReportIdList);
            apiDefinitionExecResultMapper.deleteByExample(apiDefinitionExecResultExample);
        }
    }

    public List<PlanReportCaseDTO> selectForPlanReport(List<String> apiReportIds) {
        if (CollectionUtils.isEmpty(apiReportIds)) return new ArrayList<>();
        return extApiDefinitionExecResultMapper.selectForPlanReport(apiReportIds);
    }

    private void initReportRequest(QueryAPIReportRequest request) {
        if (request != null) {
            if (MapUtils.isNotEmpty(request.getFilters()) && request.getFilters().containsKey("trigger_mode") && CollectionUtils.isNotEmpty(request.getFilters().get("trigger_mode"))) {
                boolean filterHasApi = false;
                for (String triggerMode : request.getFilters().get("trigger_mode")) {
                    if (StringUtils.equalsIgnoreCase(triggerMode, "api")) {
                        filterHasApi = true;
                    }
                }
                if (filterHasApi) {
                    request.getFilters().get("trigger_mode").add("JENKINS");
                }
            }
        }
    }
}
