package io.metersphere.service;

import io.metersphere.base.domain.ApiCaseExecutionInfo;
import io.metersphere.base.domain.ApiCaseExecutionInfoExample;
import io.metersphere.base.mapper.ApiCaseExecutionInfoMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApiCaseExecutionInfoService {
    @Resource
    private ApiCaseExecutionInfoMapper apiCaseExecutionInfoMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

    @Lazy
    public void insertExecutionInfo(String apiCaseId, String result, String triggerMode) {
        if (StringUtils.isNotEmpty(apiCaseId) && StringUtils.isNotEmpty(result)) {
            ApiCaseExecutionInfo executionInfo = new ApiCaseExecutionInfo();
            executionInfo.setResult(result);
            executionInfo.setSourceId(apiCaseId);
            executionInfo.setId(UUID.randomUUID().toString());
            executionInfo.setCreateTime(System.currentTimeMillis());
            executionInfo.setTriggerMode(triggerMode);
            apiCaseExecutionInfoMapper.insert(executionInfo);
        }
    }

    public void deleteByApiCaseId(String resourceId) {
        ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
        example.createCriteria().andSourceIdEqualTo(resourceId);
        apiCaseExecutionInfoMapper.deleteByExample(example);
    }

    public void deleteByApiCaseIdList(List<String> resourceIdList) {
        if (CollectionUtils.isNotEmpty(resourceIdList)) {
            ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
            example.createCriteria().andSourceIdIn(resourceIdList);
            apiCaseExecutionInfoMapper.deleteByExample(example);
        }
    }

    public void deleteByApiDefeinitionIdList(List<String> apiIdList) {
        if (CollectionUtils.isNotEmpty(apiIdList)) {
            List<String> apiCaseIdList = extApiTestCaseMapper.selectCaseIdsByApiIds(apiIdList);
            this.deleteByApiCaseIdList(apiCaseIdList);
        }
    }
}
