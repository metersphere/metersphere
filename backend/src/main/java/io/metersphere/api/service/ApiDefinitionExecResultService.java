package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExample;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.SessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionExecResultService {
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;


    public void saveApiResult(TestResult result, String type) {
        result.getScenarios().get(0).getRequestResults().forEach(item -> {
            // 清理原始资源，每个执行 保留一条结果
            extApiDefinitionExecResultMapper.deleteByResourceId(item.getName());
            ApiDefinitionExecResult saveResult = new ApiDefinitionExecResult();
            saveResult.setId(UUID.randomUUID().toString());
            saveResult.setCreateTime(System.currentTimeMillis());
            saveResult.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
            saveResult.setName(item.getName());
            saveResult.setResourceId(item.getName());
            saveResult.setContent(JSON.toJSONString(item));
            saveResult.setStartTime(item.getStartTime());
            saveResult.setType(type);
            saveResult.setEndTime(item.getResponseResult().getResponseTime());
            saveResult.setStatus(item.getResponseResult().getResponseCode().equals("200") ? "success" : "error");
            apiDefinitionExecResultMapper.insert(saveResult);
        });
    }

    public void deleteByResourceId(String resourceId) {
        ApiDefinitionExecResultExample example =  new ApiDefinitionExecResultExample();
        example.createCriteria().andResourceIdEqualTo(resourceId);
        apiDefinitionExecResultMapper.deleteByExample(example);
    }

    public void deleteByResourceIds(List<String> ids) {
        ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
        example.createCriteria().andResourceIdIn(ids);
        apiDefinitionExecResultMapper.deleteByExample(example);
    }

    public long countByTestCaseIDInProjectAndExecutedInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if(firstTime==null || lastTime == null){
            return  0;
        }else {
            return extApiDefinitionExecResultMapper.countByProjectIDAndCreateInThisWeek(projectId,firstTime.getTime(),lastTime.getTime());
        }
    }

    public long countByTestCaseIDInProject(String projectId) {
        return extApiDefinitionExecResultMapper.countByTestCaseIDInProject(projectId);

    }

    public List<ExecutedCaseInfoResult> findFaliureCaseInfoByProjectIDAndLimitNumberInSevenDays(String projectId, int limitNumber) {

        //获取7天之前的日期
        Date startDay = DateUtils.dateSum(new Date(),-6);
        //将日期转化为 00:00:00 的时间戳
        Date startTime = null;
        try{
            startTime = DateUtils.getDayStartTime(startDay);
        }catch (Exception e){
        }

        if(startTime==null){
            return  new ArrayList<>(0);
        }else {
            return extApiDefinitionExecResultMapper.findFaliureCaseInfoByProjectIDAndExecuteTimeAndLimitNumber(projectId,startTime.getTime(),limitNumber);
        }
    }
}
