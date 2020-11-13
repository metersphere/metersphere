package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.jmeter.ApiTestResult;
import io.metersphere.base.domain.ApiDelimitExecResult;
import io.metersphere.base.mapper.ApiDelimitExecResultMapper;
import io.metersphere.commons.utils.SessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDelimitExecResultService {
    @Resource
    private ApiDelimitExecResultMapper apiDelimitExecResultMapper;


    public void saveApiResult(ApiTestResult result) {
        result.getRequestResults().forEach(item -> {
            // 清理原始资源，每个执行 保留一条结果
            apiDelimitExecResultMapper.deleteByResourceId(item.getName());
            ApiDelimitExecResult saveResult = new ApiDelimitExecResult();
            saveResult.setId(UUID.randomUUID().toString());
            saveResult.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
            saveResult.setName(item.getUrl());
            saveResult.setResourceId(item.getName());
            saveResult.setContent(JSON.toJSONString(item));
            saveResult.setStartTime(item.getStartTime());
            saveResult.setEndTime(item.getResponseResult().getResponseTime());
            saveResult.setStatus(item.getResponseResult().getResponseCode().equals("200") ? "success" : "error");
            apiDelimitExecResultMapper.insert(saveResult);
        });
    }
}
