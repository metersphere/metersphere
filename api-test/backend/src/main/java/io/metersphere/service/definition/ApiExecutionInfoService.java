package io.metersphere.service.definition;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiCaseExecutionInfoMapper;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiExecutionInfoMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.enums.ExecutionExecuteTypeEnum;
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
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;

    @Lazy
    public void insertExecutionInfo(ApiDefinitionExecResult result) {
        if (result != null && StringUtils.isNotEmpty(result.getStatus()) && StringUtils.isNotEmpty(result.getId())) {
            String resourceID = result.getResourceId();
            if (resourceID == null) {
                resourceID = extApiDefinitionExecResultMapper.selectResourceId(result.getId());
            }
            ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(resourceID);
            if (apiDefinition != null) {
                this.insertApiExecutionInfo(apiDefinition.getId(), result.getStatus(), result.getProjectId(), ExecutionExecuteTypeEnum.BASIC.name(), apiDefinition.getVersionId());
            } else {
                ApiDefinition apiBasieInfoByCaseId = extApiTestCaseMapper.selectApiBasicInfoByCaseId(resourceID);
                if (apiBasieInfoByCaseId != null) {
                    this.insertApiCaseExecutionInfo(resourceID, result.getStatus(), result.getTriggerMode(), result.getProjectId(), ExecutionExecuteTypeEnum.BASIC.name(), apiBasieInfoByCaseId.getVersionId());
                } else {
                    String apiCaseIdInTestPlan = extTestPlanApiCaseMapper.getApiTestCaseIdById(resourceID);
                    if (StringUtils.isNotEmpty(apiCaseIdInTestPlan)) {
                        apiBasieInfoByCaseId = extApiTestCaseMapper.selectApiBasicInfoByCaseId(apiCaseIdInTestPlan);
                        if (apiBasieInfoByCaseId != null) {
                            this.insertApiCaseExecutionInfo(resourceID, result.getStatus(), result.getTriggerMode(), result.getProjectId(), ExecutionExecuteTypeEnum.TEST_PLAN.name(), apiBasieInfoByCaseId.getVersionId());
                        }
                    }
                }
            }
        }
    }

    private void insertApiCaseExecutionInfo(String resourceID, String status, String triggerMode, String projectId, String executeType, String versionId) {
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

    private void insertApiExecutionInfo(String resourceID, String status, String projectId, String executeType, String versionId) {
        ApiExecutionInfo info = new ApiExecutionInfo();
        info.setId(UUID.randomUUID().toString());
        info.setSourceId(resourceID);
        info.setCreateTime(System.currentTimeMillis());
        info.setResult(status);
        info.setProjectId(projectId);
        info.setExecuteType(executeType);
        info.setVersion(versionId);
        apiExecutionInfoMapper.insert(info);
    }

    public void deleteByProjectId(String projectId) {
        if (StringUtils.isNotEmpty(projectId)) {
            ApiExecutionInfoExample example = new ApiExecutionInfoExample();
            example.createCriteria().andProjectIdEqualTo(projectId);
            apiExecutionInfoMapper.deleteByExample(example);
        }
    }

    public void updateProjectIdByApiIdAndProjectIdIsNull(String projectId, String executeType, String version, String apiId) {
        if (StringUtils.isNoneEmpty(projectId, executeType, apiId)) {
            ApiExecutionInfoExample example = new ApiExecutionInfoExample();
            example.createCriteria().andProjectIdIsNull().andSourceIdEqualTo(apiId);
            ApiExecutionInfo updateModel = new ApiExecutionInfo();
            updateModel.setProjectId(projectId);
            updateModel.setExecuteType(executeType);
            updateModel.setVersion(version);
            apiExecutionInfoMapper.updateByExampleSelective(updateModel, example);
        }
    }

    public void deleteBySourceIdAndProjectIdIsNull(String sourceId) {
        ApiExecutionInfoExample example = new ApiExecutionInfoExample();
        example.createCriteria().andSourceIdEqualTo(sourceId).andProjectIdIsNull();
        apiExecutionInfoMapper.deleteByExample(example);
    }

    public List<String> selectSourceIdByProjectIdIsNull() {
        return extApiDefinitionMapper.selectApiIdInExecutionInfoByProjectIdIsNull();
    }

    public long countSourceIdByProjectIdIsNull() {
        return extApiDefinitionMapper.countSourceIdByProjectIdIsNull();
    }
}
