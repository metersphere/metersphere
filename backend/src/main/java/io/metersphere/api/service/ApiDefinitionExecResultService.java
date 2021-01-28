package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExample;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestPlanApiCaseService;
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
    SqlSessionFactory sqlSessionFactory;

    public void saveApiResult(TestResult result, String type) {
        if (CollectionUtils.isNotEmpty(result.getScenarios())) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiDefinitionExecResultMapper definitionExecResultMapper = sqlSession.getMapper(ApiDefinitionExecResultMapper.class);
            result.getScenarios().get(0).getRequestResults().forEach(item -> {
                ApiDefinitionExecResult saveResult = new ApiDefinitionExecResult();
                saveResult.setId(UUID.randomUUID().toString());
                saveResult.setCreateTime(System.currentTimeMillis());
                saveResult.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
                saveResult.setName(item.getName());
                if (item.getName().indexOf("<->") != -1) {
                    saveResult.setName(item.getName().substring(0, item.getName().indexOf("<->")));
                }
                saveResult.setResourceId(item.getName());
                saveResult.setContent(JSON.toJSONString(item));
                saveResult.setStartTime(item.getStartTime());
                String status = item.isSuccess() ? "success" : "error";
                saveResult.setEndTime(item.getResponseResult().getResponseTime());
                saveResult.setType(type);
                saveResult.setStatus(status);
                if (StringUtils.equals(type, ApiRunMode.API_PLAN.name())) {
                    testPlanApiCaseService.setExecResult(item.getName(), status);
                }
                // 更新用例最后执行结果
                ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = new ApiTestCaseWithBLOBs();
                apiTestCaseWithBLOBs.setId(saveResult.getResourceId());
                apiTestCaseWithBLOBs.setLastResultId(saveResult.getId());

                apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCaseWithBLOBs);
                definitionExecResultMapper.insert(saveResult);
            });
            sqlSession.flushStatements();
        }
    }

    /**
     * 定时任务触发的保存逻辑
     * 定时任务时，userID要改为定时任务中的用户
     *
     * @param result
     * @param type
     */
    public void saveApiResultByScheduleTask(TestResult result, String type) {
        String saveResultType = type;
        if(StringUtils.equalsAny(ApiRunMode.SCHEDULE_API_PLAN.name(),saveResultType)){
            saveResultType = ApiRunMode.API_PLAN.name();
        }

        String finalSaveResultType = saveResultType;
        result.getScenarios().get(0).getRequestResults().forEach(item -> {
            ApiDefinitionExecResult saveResult = new ApiDefinitionExecResult();
            saveResult.setId(UUID.randomUUID().toString());
            saveResult.setCreateTime(System.currentTimeMillis());
            saveResult.setName(item.getName());
            saveResult.setResourceId(item.getName());
            saveResult.setContent(JSON.toJSONString(item));
            saveResult.setStartTime(item.getStartTime());
            String status = item.isSuccess() ? "success" : "error";
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
            } else {
                userID = Objects.requireNonNull(SessionUtils.getUser()).getId();
                testPlanApiCaseService.setExecResult(item.getName(), status);
            }

            saveResult.setUserId(userID);
            apiDefinitionExecResultMapper.insert(saveResult);
        });
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
