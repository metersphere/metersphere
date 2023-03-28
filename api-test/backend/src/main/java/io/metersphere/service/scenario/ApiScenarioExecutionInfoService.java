package io.metersphere.service.scenario;

import io.metersphere.api.dto.automation.InsertExecutionInfoDTO;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ScenarioExecutionInfoMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.enums.ExecutionExecuteTypeEnum;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.ResultDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApiScenarioExecutionInfoService {
    @Resource
    private ScenarioExecutionInfoMapper scenarioExecutionInfoMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;

    @Lazy
    public void insertExecutionInfo(InsertExecutionInfoDTO executionSaveDTO) {
        if (StringUtils.isNotEmpty(executionSaveDTO.getSourceId())
                && StringUtils.isNotEmpty(executionSaveDTO.getExecResult())
                && StringUtils.isNotEmpty(executionSaveDTO.getExecReportId())) {
            //通过场景ID和报告ID检查执行有没有被记录
            if (!this.hasExecutionRecorded(executionSaveDTO.getSourceId(), executionSaveDTO.getExecReportId())) {
                ScenarioExecutionInfo executionInfo = new ScenarioExecutionInfo();
                executionInfo.setResult(executionSaveDTO.getExecResult());
                executionInfo.setSourceId(executionSaveDTO.getSourceId());
                executionInfo.setId(UUID.randomUUID().toString());
                executionInfo.setCreateTime(System.currentTimeMillis());
                executionInfo.setTriggerMode(executionSaveDTO.getTriggerMode());
                executionInfo.setProjectId(executionSaveDTO.getProjectId());
                executionInfo.setExecuteType(executionSaveDTO.getExecuteType());
                executionInfo.setVersion(executionSaveDTO.getVersion());
                executionInfo.setReportId(executionSaveDTO.getExecReportId());
                scenarioExecutionInfoMapper.insert(executionInfo);
            }
        }
    }

    public boolean hasExecutionRecorded(String sourceId, String reportId) {
        ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
        example.createCriteria().andSourceIdEqualTo(sourceId).andReportIdEqualTo(reportId);
        return scenarioExecutionInfoMapper.countByExample(example) > 0;
    }

    @Lazy
    public void insertScenarioInfo(ApiScenarioWithBLOBs apiScenario, ApiScenarioReport scenarioReport, ResultDTO dto) {
        if (ObjectUtils.anyNull(apiScenario, scenarioReport, dto)) {
            return;
        }
        if (StringUtils.equalsAnyIgnoreCase(dto.getRunMode(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            InsertExecutionInfoDTO saveDTO = new InsertExecutionInfoDTO(
                    dto.getTestId(),
                    scenarioReport.getStatus(),
                    scenarioReport.getTriggerMode(),
                    scenarioReport.getProjectId() == null ? apiScenario.getProjectId() : scenarioReport.getProjectId(),
                    ExecutionExecuteTypeEnum.TEST_PLAN.name(),
                    apiScenario.getVersionId(),
                    scenarioReport.getId());
            this.insertExecutionInfo(saveDTO);
        } else {
            InsertExecutionInfoDTO saveDTO = new InsertExecutionInfoDTO(
                    scenarioReport.getScenarioId(),
                    scenarioReport.getStatus(),
                    scenarioReport.getTriggerMode(),
                    scenarioReport.getProjectId() == null ? apiScenario.getProjectId() : scenarioReport.getProjectId(),
                    ExecutionExecuteTypeEnum.BASIC.name(),
                    apiScenario.getVersionId(),
                    scenarioReport.getId());
            this.insertExecutionInfo(saveDTO);
        }
    }

    public void insertExecutionInfoByScenarioList(List<ApiScenario> apiScenarios, String status, String triggerMode, String projectId, String executeType, String reportId) {
        for (ApiScenario apiScenario : apiScenarios) {
            InsertExecutionInfoDTO saveDTO = new InsertExecutionInfoDTO(
                    apiScenario.getId(),
                    status,
                    triggerMode,
                    projectId,
                    executeType,
                    apiScenario.getVersionId(),
                    reportId);
            this.insertExecutionInfo(saveDTO);
        }
    }

    public void insertExecutionInfoByScenarioIds(String scenarioIdJsonString, String status, String triggerMode, String projectId, String executeType, String reportId) {
        try {
            List<String> scenarioIdList = JSON.parseArray(scenarioIdJsonString, String.class);
            if (CollectionUtils.isNotEmpty(scenarioIdList)) {
                ApiScenarioExample example = new ApiScenarioExample();
                example.createCriteria().andIdIn(scenarioIdList);
                List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);
                this.insertExecutionInfoByScenarioList(apiScenarios, status, triggerMode, projectId, executeType, reportId);
            }
        } catch (Exception e) {
            LogUtil.error("解析场景ID的JSON" + scenarioIdJsonString + "失败！", e);
        }
    }

    public void deleteByProjectId(String projectId) {
        if (StringUtils.isNotEmpty(projectId)) {
            ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
            example.createCriteria().andProjectIdEqualTo(projectId);
            scenarioExecutionInfoMapper.deleteByExample(example);
        }
    }

    public void updateProjectIdBySourceIdAndProjectIdIsNull(String projectId, String executeType, String version, String apiId) {
        if (StringUtils.isNoneEmpty(projectId, executeType, apiId)) {
            ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
            example.createCriteria().andProjectIdIsNull().andSourceIdEqualTo(apiId);
            ScenarioExecutionInfo updateModel = new ScenarioExecutionInfo();
            updateModel.setProjectId(projectId);
            updateModel.setExecuteType(executeType);
            updateModel.setVersion(version);
            scenarioExecutionInfoMapper.updateByExampleSelective(updateModel, example);
        }
    }

    public void deleteBySourceIdAndProjectIdIsNull(String sourceId) {
        ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
        example.createCriteria().andSourceIdEqualTo(sourceId).andProjectIdIsNull();
        scenarioExecutionInfoMapper.deleteByExample(example);
    }

    public long countExecuteTimesByProjectID(String projectId, String triggerMode, String executeType, String version) {
        ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
        ScenarioExecutionInfoExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        if (StringUtils.isNotEmpty(triggerMode)) {
            criteria.andTriggerModeEqualTo(triggerMode);
        }
        if (StringUtils.isNotEmpty(version)) {
            criteria.andVersionEqualTo(version);
        }
        if (StringUtils.isNotEmpty(executeType)) {
            criteria.andExecuteTypeEqualTo(executeType);
        }
        return scenarioExecutionInfoMapper.countByExample(example);
    }

    public List<String> selectSourceIdByProjectIdIsNull() {
        return extApiScenarioMapper.selectScenarioIdInExecutionInfoByProjectIdIsNull();
    }

    public long countSourceIdByProjectIdIsNull() {
        return extApiScenarioMapper.countSourceIdByProjectIdIsNull();
    }
}
