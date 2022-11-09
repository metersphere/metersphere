package io.metersphere.service.definition;

import io.metersphere.base.domain.ApiCaseExecutionInfo;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiExecutionInfo;
import io.metersphere.base.domain.ApiExecutionInfoExample;
import io.metersphere.base.mapper.ApiCaseExecutionInfoMapper;
import io.metersphere.base.mapper.ApiExecutionInfoMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.enums.ExecutionExecuteTypeEnum;
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
                this.insertApiExecutionInfo(resourceID, result.getStatus(), result.getProjectId(), ExecutionExecuteTypeEnum.BASIC.name());
            } else {
                boolean isApiCase = extApiTestCaseMapper.countById(resourceID) > 0;
                if (isApiCase) {
                    this.insertApiCaseExecutionInfo(resourceID, result.getStatus(), result.getTriggerMode(), result.getProjectId(), ExecutionExecuteTypeEnum.BASIC.name());
                } else {
                    String apiCaseIdInTestPlan = extTestPlanApiCaseMapper.getApiTestCaseIdById(resourceID);
                    if (StringUtils.isNotEmpty(apiCaseIdInTestPlan)) {
                        this.insertApiCaseExecutionInfo(resourceID, result.getStatus(), result.getTriggerMode(), result.getProjectId(), ExecutionExecuteTypeEnum.TEST_PLAN.name());
                    }
                }
            }
        }
    }

    private void insertApiCaseExecutionInfo(String resourceID, String status, String triggerMode, String projectId, String executeType) {
        ApiCaseExecutionInfo info = new ApiCaseExecutionInfo();
        info.setId(UUID.randomUUID().toString());
        info.setSourceId(resourceID);
        info.setCreateTime(System.currentTimeMillis());
        info.setResult(status);
        info.setTriggerMode(triggerMode);
        info.setProjectId(projectId);
        info.setExecuteType(executeType);
        apiCaseExecutionInfoMapper.insert(info);
    }

    private void insertApiExecutionInfo(String resourceID, String status, String projectId, String executeType) {
        ApiExecutionInfo info = new ApiExecutionInfo();
        info.setId(UUID.randomUUID().toString());
        info.setSourceId(resourceID);
        info.setCreateTime(System.currentTimeMillis());
        info.setResult(status);
        info.setProjectId(projectId);
        info.setExecuteType(executeType);
        apiExecutionInfoMapper.insert(info);
    }

    public void deleteByProjectId(String projectId) {
        if (StringUtils.isNotEmpty(projectId)) {
            ApiExecutionInfoExample example = new ApiExecutionInfoExample();
            example.createCriteria().andProjectIdEqualTo(projectId);
            apiExecutionInfoMapper.deleteByExample(example);
        }
    }

    public List<ApiExecutionInfo> selectByProjectIdIsNull() {
        ApiExecutionInfoExample example = new ApiExecutionInfoExample();
        example.createCriteria().andProjectIdIsNull();
        return apiExecutionInfoMapper.selectByExample(example);
    }

    public void updateProjectIdByApiIdAndProjectIdIsNull(String projectId, String executeType, String apiId) {
        if (StringUtils.isNoneEmpty(projectId, executeType, apiId)) {
            ApiExecutionInfoExample example = new ApiExecutionInfoExample();
            example.createCriteria().andProjectIdIsNull().andSourceIdEqualTo(apiId);
            ApiExecutionInfo updateModel = new ApiExecutionInfo();
            updateModel.setProjectId(projectId);
            updateModel.setExecuteType(executeType);

            apiExecutionInfoMapper.updateByExampleSelective(updateModel, example);
        }
    }

    public void deleteByIds(List<String> deleteIdList) {
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            ApiExecutionInfoExample example = new ApiExecutionInfoExample();
            example.createCriteria().andIdIn(deleteIdList);
            apiExecutionInfoMapper.deleteByExample(example);
        }
    }
}
