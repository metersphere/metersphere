package io.metersphere.service;

import io.metersphere.base.domain.ApiCaseExecutionInfo;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiExecutionInfo;
import io.metersphere.base.domain.ApiExecutionInfoExample;
import io.metersphere.base.mapper.ApiCaseExecutionInfoMapper;
import io.metersphere.base.mapper.ApiExecutionInfoMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
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
public class ApiExecutionInfoService {
    @Resource
    private ApiExecutionInfoMapper apiExecutionInfoMapper;
    @Resource
    private ApiCaseExecutionInfoMapper apiCaseExecutionInfoMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;

    @Lazy
    public void insertExecutionInfo(ApiDefinitionExecResult result) {
        if (result != null && StringUtils.isNotEmpty(result.getStatus()) && StringUtils.isNotEmpty(result.getId())) {
            String resourceID = result.getResourceId();
            if (resourceID == null) {
                resourceID = extApiDefinitionExecResultMapper.selectResourceId(result.getId());
            }
            boolean isApiDefinition = extApiDefinitionMapper.countById(resourceID) > 0;
            if (isApiDefinition) {
                this.insertApiExecutionInfo(resourceID, result.getStatus());
            } else {
                boolean isApiCase = extApiTestCaseMapper.countById(resourceID) > 0;
                if (isApiCase) {
                    this.insertApiCaseExecutionInfo(resourceID, result.getStatus(), result.getTriggerMode());
                } else {
                    String apiCaseIdInTestPlan = extTestPlanApiCaseMapper.getApiTestCaseIdById(resourceID);
                    if (StringUtils.isNotEmpty(apiCaseIdInTestPlan)) {
                        this.insertApiCaseExecutionInfo(resourceID, result.getStatus(), result.getTriggerMode());
                    }
                }
            }
        }
    }

    private void insertApiCaseExecutionInfo(String resourceID, String status, String triggerMode) {
        ApiCaseExecutionInfo info = new ApiCaseExecutionInfo();
        info.setId(UUID.randomUUID().toString());
        info.setSourceId(resourceID);
        info.setCreateTime(System.currentTimeMillis());
        info.setResult(status);
        info.setTriggerMode(triggerMode);
        apiCaseExecutionInfoMapper.insert(info);
    }

    private void insertApiExecutionInfo(String resourceID, String status) {
        ApiExecutionInfo info = new ApiExecutionInfo();
        info.setId(UUID.randomUUID().toString());
        info.setSourceId(resourceID);
        info.setCreateTime(System.currentTimeMillis());
        info.setResult(status);
        apiExecutionInfoMapper.insert(info);
    }

    public void deleteByApiId(String resourceId) {
        ApiExecutionInfoExample example = new ApiExecutionInfoExample();
        example.createCriteria().andSourceIdEqualTo(resourceId);
        apiExecutionInfoMapper.deleteByExample(example);
    }

    public void deleteByApiIdList(List<String> resourceIdList) {
        if (CollectionUtils.isNotEmpty(resourceIdList)) {
            ApiExecutionInfoExample example = new ApiExecutionInfoExample();
            example.createCriteria().andSourceIdIn(resourceIdList);
            apiExecutionInfoMapper.deleteByExample(example);
        }
    }
}
