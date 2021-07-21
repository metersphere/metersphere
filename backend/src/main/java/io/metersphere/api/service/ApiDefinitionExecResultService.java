package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestCaseReviewApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestCaseReviewApiCaseService;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanReportService;
import io.metersphere.track.service.TestPlanService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionExecResultService {
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
    SqlSessionFactory sqlSessionFactory;

    public ApiDefinitionExecResult getInfo(String id) {
        return apiDefinitionExecResultMapper.selectByPrimaryKey(id);
    }

    public void saveApiResult(TestResult result, String type, String triggerMode) {
        if (CollectionUtils.isNotEmpty(result.getScenarios())) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiDefinitionExecResultMapper definitionExecResultMapper = sqlSession.getMapper(ApiDefinitionExecResultMapper.class);
            result.getScenarios().forEach(scenarioResult -> {
                if (scenarioResult != null && CollectionUtils.isNotEmpty(scenarioResult.getRequestResults())) {
                    scenarioResult.getRequestResults().forEach(item -> {
                        ApiDefinitionExecResult saveResult = definitionExecResultMapper.selectByPrimaryKey(result.getTestId());
                        item.getResponseResult().setConsole(result.getConsole());
                        boolean saved = true;
                        if (saveResult == null) {
                            saveResult = new ApiDefinitionExecResult();
                            saveResult.setId(result.getTestId());
                            saveResult.setActuator("LOCAL");
                            saveResult.setName(item.getName());
                            saveResult.setTriggerMode(triggerMode);
                            saveResult.setType(type);
                            if (StringUtils.isNotEmpty(result.getUserId())) {
                                saveResult.setUserId(result.getUserId());
                            } else {
                                saveResult.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
                            }
                            saved = false;
                        }
                        String status = item.isSuccess() ? "success" : "error";
                        saveResult.setName(getName(type, item.getName(), status, saveResult.getCreateTime()));
                        saveResult.setStatus(status);
                        saveResult.setCreateTime(item.getStartTime());
                        saveResult.setResourceId(item.getName());
                        saveResult.setContent(JSON.toJSONString(item));
                        saveResult.setStartTime(item.getStartTime());
                        saveResult.setEndTime(item.getResponseResult().getResponseTime());

                        // 清空上次执行结果的内容，只保留当前最新一条内容
                        ApiDefinitionExecResult prevResult = extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(item.getName(), type);
                        if (prevResult != null) {
                            prevResult.setContent(null);
                            definitionExecResultMapper.updateByPrimaryKeyWithBLOBs(prevResult);
                        }
                        // 更新用例最后执行结果
                        ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = new ApiTestCaseWithBLOBs();
                        apiTestCaseWithBLOBs.setId(saveResult.getResourceId());
                        apiTestCaseWithBLOBs.setLastResultId(saveResult.getId());

                        if (StringUtils.isNotEmpty(saveResult.getTriggerMode()) && saveResult.getTriggerMode().equals("CASE")) {
                            saveResult.setTriggerMode(TriggerMode.MANUAL.name());
                        }
                        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCaseWithBLOBs);
                        if (!saved) {
                            definitionExecResultMapper.insert(saveResult);
                        } else {
                            definitionExecResultMapper.updateByPrimaryKeyWithBLOBs(saveResult);
                        }
                    });
                }
            });
            sqlSession.flushStatements();
        }
    }

    private String getName(String type, String id, String status, Long time) {
        if (id.indexOf(DelimiterConstants.SEPARATOR.toString()) != -1) {
            return id.substring(0, id.indexOf(DelimiterConstants.SEPARATOR.toString()));
        }
        if (StringUtils.equalsAnyIgnoreCase(type, ApiRunMode.API_PLAN.name(),ApiRunMode.SCHEDULE_API_PLAN.name(),ApiRunMode.JENKINS_API_PLAN.name())) {
            TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(id);
            ApiTestCaseWithBLOBs caseWithBLOBs = null;
            if (testPlanApiCase != null) {
                testPlanApiCaseService.setExecResult(id, status, time);
                caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(testPlanApiCase.getApiCaseId());
            }
            TestCaseReviewApiCase testCaseReviewApiCase = testCaseReviewApiCaseMapper.selectByPrimaryKey(id);
            if (testCaseReviewApiCase != null) {
                testCaseReviewApiCaseService.setExecResult(id, status, time);
                caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(testCaseReviewApiCase.getApiCaseId());
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
    public void saveApiResultByScheduleTask(TestResult result,String testPlanReportId, String type,String trigeMode) {
        String saveResultType = type;
        if (StringUtils.equalsAny(saveResultType, ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
            saveResultType = ApiRunMode.API_PLAN.name();
        }
        String finalSaveResultType = saveResultType;

        Map<String,String> apiIdResultMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(result.getScenarios())) {
            result.getScenarios().forEach(scenarioResult -> {
                if (scenarioResult != null && CollectionUtils.isNotEmpty(scenarioResult.getRequestResults())) {
                    scenarioResult.getRequestResults().forEach(item -> {
                        String status = item.isSuccess() ? "success" : "error";
                        ApiDefinitionExecResult saveResult = new ApiDefinitionExecResult();
                        saveResult.setId(UUID.randomUUID().toString());
                        saveResult.setCreateTime(System.currentTimeMillis());
//                        saveResult.setName(item.getName());
                        saveResult.setName(getName(type, item.getName(), status, saveResult.getCreateTime()));
                        ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionMapper.selectByPrimaryKey(item.getName());
                        if (apiDefinitionWithBLOBs != null) {
                            saveResult.setName(apiDefinitionWithBLOBs.getName());
                            apiIdResultMap.put(apiDefinitionWithBLOBs.getId(),item.isSuccess()? TestPlanApiExecuteStatus.SUCCESS.name() : TestPlanApiExecuteStatus.FAILD.name());
                        } else {
                            ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(item.getName());
                            if (caseWithBLOBs != null) {
                                saveResult.setName(caseWithBLOBs.getName());
                                apiIdResultMap.put(caseWithBLOBs.getId(),item.isSuccess()? TestPlanApiExecuteStatus.SUCCESS.name() : TestPlanApiExecuteStatus.FAILD.name());
                            }else {
                                caseWithBLOBs = testPlanApiCaseService.getApiTestCaseById(item.getName());
                                if (caseWithBLOBs != null) {
                                    saveResult.setName(caseWithBLOBs.getName());
                                    apiIdResultMap.put(caseWithBLOBs.getId(), item.isSuccess() ? TestPlanApiExecuteStatus.SUCCESS.name() : TestPlanApiExecuteStatus.FAILD.name());
                                }
                            }
                        }
                        if (StringUtils.equals(type, ApiRunMode.JENKINS_API_PLAN.name())) {
                            saveResult.setTriggerMode(TriggerMode.API.name());
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

                        String userID = null;
                        if (StringUtils.equals(type, ApiRunMode.SCHEDULE_API_PLAN.name())) {
                            TestPlanApiCase apiCase = testPlanApiCaseService.getById(item.getName());
                            String scheduleCreateUser = testPlanService.findScheduleCreateUserById(apiCase.getTestPlanId());
                            userID = scheduleCreateUser;
                            apiCase.setStatus(status);
                            apiCase.setUpdateTime(System.currentTimeMillis());
                            testPlanApiCaseService.updateByPrimaryKeySelective(apiCase);
                        } else if (StringUtils.equals(type, ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                            TestPlanApiCase apiCase = testPlanApiCaseService.getById(item.getName());
                            userID = Objects.requireNonNull(SessionUtils.getUser()).getId();
                            apiCase.setStatus(status);
                            apiCase.setUpdateTime(System.currentTimeMillis());
                            testPlanApiCaseService.updateByPrimaryKeySelective(apiCase);
                        } else {
                            userID = Objects.requireNonNull(SessionUtils.getUser()).getId();
                            testPlanApiCaseService.setExecResult(item.getName(), status, item.getStartTime());
                            testCaseReviewApiCaseService.setExecResult(item.getName(), status, item.getStartTime());
                        }
                        saveResult.setUserId(userID);
                        // 前一条数据内容清空
                        ApiDefinitionExecResult prevResult = extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(item.getName(), finalSaveResultType);
                        if (prevResult != null) {
                            prevResult.setContent(null);
                            apiDefinitionExecResultMapper.updateByPrimaryKeyWithBLOBs(prevResult);
                        }
                        apiDefinitionExecResultMapper.insert(saveResult);
                    });
                }
            });
        }

        TestPlanReportService testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
        testPlanReportService.updateExecuteApis(testPlanReportId,apiIdResultMap,null,null);
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
