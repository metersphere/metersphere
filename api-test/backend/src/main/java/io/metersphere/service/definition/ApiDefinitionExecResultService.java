package io.metersphere.service.definition;

import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.RequestResultExpandDTO;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.plan.TestPlanApiCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.MsHashTreeService;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.service.ServiceUtils;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
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
    private RedisTemplateService redisTemplateService;

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

    public void batchSaveApiResult(List<ResultDTO> resultDTOS) {
        if (CollectionUtils.isEmpty(resultDTOS)) {
            LoggerUtil.info("未接收到处理结果 ");
            return;
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionExecResultMapper batchExecResultMapper = sqlSession.getMapper(ApiDefinitionExecResultMapper.class);
        ApiTestCaseMapper batchApiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        TestPlanApiCaseMapper planApiCaseMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);

        for (ResultDTO dto : resultDTOS) {
            this.mergeRetryResults(dto);
            LoggerUtil.info("开始存储报告结果[ " + dto.getRequestResults().size() + " ]", dto.getReportId());
            if (CollectionUtils.isEmpty(dto.getRequestResults())) {
                LoggerUtil.info("未解析到执行结果", dto.getReportId());
                continue;
            }
            // 过滤掉全局脚本
            List<RequestResult> requestResults = dto.getRequestResults().stream()
                    .filter(item -> !StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_"))
                    .collect(Collectors.toList());

            for (RequestResult item : requestResults) {
                ApiDefinitionExecResultWithBLOBs result = this.initResult(item, dto);
                if (StringUtils.isBlank(result.getProjectId()) && dto.getExtendedParameters().containsKey(MsHashTreeService.PROJECT_ID)) {
                    result.setProjectId(this.getProjectId(dto.getExtendedParameters().get(MsHashTreeService.PROJECT_ID).toString()));
                }
                result.setResourceId(dto.getTestId());
                // 更新结果数据
                batchExecResultMapper.updateByPrimaryKeySelective(result);

                apiExecutionInfoService.insertExecutionInfo(result);
                // 批量更新关联关系状态
                batchEditStatus(result.getStatus(), result.getId(), dto, planApiCaseMapper, batchApiTestCaseMapper);
                // 发送通知
                if (!StringUtils.startsWithAny(dto.getRunMode(), NoticeConstants.Mode.SCHEDULE)) {
                    User user = getUser(dto, result);
                    if (MapUtils.isNotEmpty(dto.getExtendedParameters())
                            && dto.getExtendedParameters().containsKey(CommonConstants.USER)
                            && dto.getExtendedParameters().get(CommonConstants.USER) instanceof User) {
                        user = (User) dto.getExtendedParameters().get(CommonConstants.USER);
                    }
                    // 发送通知
                    result.setResourceId(dto.getTestId());
                    sendNotice(result, user);
                }
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private String getProjectId(String projectIdFromResultDTO) {
        String returnStr = projectIdFromResultDTO;
        if (StringUtils.startsWith(projectIdFromResultDTO, "[") && StringUtils.endsWith(projectIdFromResultDTO, "]")) {
            try {
                List<String> projectIdList = JSON.parseArray(projectIdFromResultDTO, String.class);
                returnStr = projectIdList.get(0);
            } catch (Exception ignore) {
            }
        }
        return returnStr;
    }

    private User getUser(ResultDTO dto, ApiDefinitionExecResult result) {
        User user = null;
        if (MapUtils.isNotEmpty(dto.getExtendedParameters())) {
            if (dto.getExtendedParameters().containsKey(CommonConstants.USER_ID)
                    && dto.getExtendedParameters().containsKey(CommonConstants.USER_NAME)) {
                user = new User() {{
                    this.setId(dto.getExtendedParameters().get(CommonConstants.USER_ID).toString());
                    this.setName(dto.getExtendedParameters().get(CommonConstants.USER_NAME).toString());
                }};
                result.setUserId(user.getId());
            } else if (dto.getExtendedParameters().containsKey(CommonConstants.USER_ID)) {
                result.setUserId(dto.getExtendedParameters().get(CommonConstants.USER_ID).toString());
            }
        }
        return user;
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
            if (result.getUserId() == null && user != null) {
                result.setUserId(user.getId());
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

    public void batchEditStatus(
            String status, String reportId, ResultDTO dto,
            TestPlanApiCaseMapper batchTestPlanApiCaseMapper,
            ApiTestCaseMapper batchApiTestCaseMapper) {
        ApiRunMode runMode = ApiRunMode.fromString(dto.getRunMode());

        switch (runMode) {
            case API_PLAN:
            case SCHEDULE_API_PLAN:
            case JENKINS_API_PLAN:
            case MANUAL_PLAN:
                TestPlanApiCase apiCase = new TestPlanApiCase();
                apiCase.setId(dto.getTestId());
                apiCase.setStatus(status);
                apiCase.setUpdateTime(System.currentTimeMillis());
                batchTestPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
                redisTemplateService.unlock(dto.getTestId(), reportId);
                break;

            default:
                ApiTestCaseWithBLOBs caseWithBLOBs = new ApiTestCaseWithBLOBs();
                caseWithBLOBs.setId(dto.getTestId());
                caseWithBLOBs.setLastResultId(reportId);
                caseWithBLOBs.setStatus(status);
                caseWithBLOBs.setUpdateTime(System.currentTimeMillis());
                batchApiTestCaseMapper.updateByPrimaryKeySelective(caseWithBLOBs);
                break;
        }
    }

    public long countByTestCaseIDInProjectAndExecutedInThisWeek(String projectId, String version) {
        Date firstTime = DateUtils.getWeedFirstTimeAndLastTime(new Date()).get("firstTime");
        Date lastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date()).get("lastTime");
        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiDefinitionExecResultMapper.countByProjectIDAndCreateInThisWeek(projectId, version, firstTime.getTime(), lastTime.getTime());
        }
    }

    private ApiDefinitionExecResultWithBLOBs initResult(RequestResult item, ResultDTO dto) {
        ApiDefinitionExecResultWithBLOBs saveResult = new ApiDefinitionExecResultWithBLOBs();
        item.getResponseResult().setConsole(dto.getConsole());
        saveResult.setId(dto.getReportId());
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
        saveResult.setType(dto.getRunMode());
        saveResult.setStatus(status);
        saveResult.setStartTime(item.getStartTime());
        saveResult.setEndTime(item.getEndTime());
        if (item.getStartTime() >= item.getEndTime()) {
            saveResult.setEndTime(System.currentTimeMillis());
        }
        if (StringUtils.isNotEmpty(saveResult.getTriggerMode()) && saveResult.getTriggerMode().equals(CommonConstants.CASE)) {
            saveResult.setTriggerMode(TriggerMode.MANUAL.name());
        }
        saveResult.setResourceId(dto.getTestId());
        return saveResult;
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

    public void updateByExampleSelective(ApiDefinitionExecResultWithBLOBs resultWithBLOBs, ApiDefinitionExecResultExample resultExample) {
        apiDefinitionExecResultMapper.updateByExampleSelective(resultWithBLOBs, resultExample);
    }

    public ApiDefinitionExecResultWithBLOBs getLastResult(String testId) {
        return extApiDefinitionExecResultMapper.selectMaxResultByResourceId(testId);
    }

    public Map<String, String> selectResultByIdList(List<String> reportIdList) {
        Map<String, String> returnMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(reportIdList)) {
            List<ApiDefinitionExecResult> apiDefinitionExecResultList = extApiDefinitionExecResultMapper.selectStatusByIdList(reportIdList);
            if (CollectionUtils.isNotEmpty(apiDefinitionExecResultList)) {
                returnMap = apiDefinitionExecResultList.stream().collect(Collectors.toMap(ApiDefinitionExecResult::getId, ApiDefinitionExecResult::getStatus, (k1, k2) -> k1));
            }
        }
        return returnMap;
    }
}
