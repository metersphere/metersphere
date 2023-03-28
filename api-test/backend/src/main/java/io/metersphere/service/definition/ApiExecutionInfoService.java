package io.metersphere.service.definition;

import io.metersphere.api.dto.automation.InsertExecutionInfoDTO;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiExecutionInfo;
import io.metersphere.base.domain.ApiExecutionInfoExample;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiExecutionInfoMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.enums.ExecutionExecuteTypeEnum;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApiExecutionInfoService {
    @Resource
    private ApiExecutionInfoMapper apiExecutionInfoMapper;
    @Resource
    private ApiCaseExecutionInfoService apiCaseExecutionInfoService;
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

    @Lazy
    public void insertExecutionInfo(ApiDefinitionExecResult result) {
        if (result != null && StringUtils.isNotEmpty(result.getStatus()) && StringUtils.isNotEmpty(result.getId())) {
            String resourceID = result.getResourceId();
            if (resourceID == null) {
                resourceID = extApiDefinitionExecResultMapper.selectResourceId(result.getId());
            }
            ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(resourceID);
            if (apiDefinition != null) {
                InsertExecutionInfoDTO executionSaveDTO = new InsertExecutionInfoDTO(
                        resourceID,
                        result.getStatus(),
                        result.getTriggerMode(),
                        result.getProjectId(),
                        ExecutionExecuteTypeEnum.BASIC.name(),
                        apiDefinition.getVersionId(),
                        result.getId());
                this.insertApiExecutionInfo(executionSaveDTO);
            } else {
                ApiDefinition apiBasieInfoByCaseId = extApiTestCaseMapper.selectApiBasicInfoByCaseId(resourceID);
                if (apiBasieInfoByCaseId != null) {
                    InsertExecutionInfoDTO executionSaveDTO = new InsertExecutionInfoDTO(
                            resourceID,
                            result.getStatus(),
                            result.getTriggerMode(),
                            result.getProjectId(),
                            ExecutionExecuteTypeEnum.BASIC.name(),
                            apiBasieInfoByCaseId.getVersionId(),
                            result.getId());
                    apiCaseExecutionInfoService.insertExecutionInfo(executionSaveDTO);
                } else {
                    String apiCaseIdInTestPlan = extTestPlanApiCaseMapper.getApiTestCaseIdById(resourceID);
                    if (StringUtils.isNotEmpty(apiCaseIdInTestPlan)) {
                        apiBasieInfoByCaseId = extApiTestCaseMapper.selectApiBasicInfoByCaseId(apiCaseIdInTestPlan);
                        if (apiBasieInfoByCaseId != null) {
                            InsertExecutionInfoDTO executionSaveDTO = new InsertExecutionInfoDTO(
                                    resourceID,
                                    result.getStatus(),
                                    result.getTriggerMode(),
                                    result.getProjectId(),
                                    ExecutionExecuteTypeEnum.TEST_PLAN.name(),
                                    apiBasieInfoByCaseId.getVersionId(),
                                    result.getId());
                            apiCaseExecutionInfoService.insertExecutionInfo(executionSaveDTO);
                        }
                    }
                }
            }
        }
    }

    private void insertApiExecutionInfo(InsertExecutionInfoDTO executionSaveDTO) {
        if (StringUtils.isNotEmpty(executionSaveDTO.getSourceId())
                && StringUtils.isNotEmpty(executionSaveDTO.getExecReportId())
                && !this.hasExecutionRecorded(executionSaveDTO.getSourceId(), executionSaveDTO.getExecReportId())) {
            ApiExecutionInfo info = new ApiExecutionInfo();
            info.setId(UUID.randomUUID().toString());
            info.setSourceId(executionSaveDTO.getSourceId());
            info.setCreateTime(System.currentTimeMillis());
            info.setResult(executionSaveDTO.getExecResult());
            info.setProjectId(executionSaveDTO.getProjectId());
            info.setExecuteType(executionSaveDTO.getExecuteType());
            info.setVersion(executionSaveDTO.getVersion());
            info.setReportId(executionSaveDTO.getExecReportId());
            apiExecutionInfoMapper.insert(info);
        }
    }

    public boolean hasExecutionRecorded(String sourceId, String reportId) {
        ApiExecutionInfoExample example = new ApiExecutionInfoExample();
        example.createCriteria().andSourceIdEqualTo(sourceId).andReportIdEqualTo(reportId);
        return apiExecutionInfoMapper.countByExample(example) > 0;
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
