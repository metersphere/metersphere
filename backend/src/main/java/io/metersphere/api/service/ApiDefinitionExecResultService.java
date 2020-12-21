package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExample;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.commons.utils.SessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionExecResultService {
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;

    public void saveApiResult(TestResult result, String type) {
        result.getScenarios().get(0).getRequestResults().forEach(item -> {
            ApiDefinitionExecResult saveResult = new ApiDefinitionExecResult();
            saveResult.setId(UUID.randomUUID().toString());
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
        ApiDefinitionExecResultExample example =  new ApiDefinitionExecResultExample();
        example.createCriteria().andResourceIdIn(ids);
        apiDefinitionExecResultMapper.deleteByExample(example);
    }
}
