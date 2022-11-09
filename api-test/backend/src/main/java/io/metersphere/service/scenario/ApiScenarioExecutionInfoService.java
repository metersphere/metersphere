package io.metersphere.service.scenario;

import io.metersphere.base.domain.ScenarioExecutionInfo;
import io.metersphere.base.domain.ScenarioExecutionInfoExample;
import io.metersphere.base.mapper.ScenarioExecutionInfoMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
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
public class ApiScenarioExecutionInfoService {
    @Resource
    private ScenarioExecutionInfoMapper scenarioExecutionInfoMapper;

    @Lazy
    public void insertExecutionInfo(String scenarioId, String result, String triggerMode, String projectId, String executeType) {
        if (StringUtils.isNotEmpty(scenarioId) && StringUtils.isNotEmpty(result)) {
            ScenarioExecutionInfo executionInfo = new ScenarioExecutionInfo();
            executionInfo.setResult(result);
            executionInfo.setSourceId(scenarioId);
            executionInfo.setId(UUID.randomUUID().toString());
            executionInfo.setCreateTime(System.currentTimeMillis());
            executionInfo.setTriggerMode(triggerMode);
            executionInfo.setProjectId(projectId);
            executionInfo.setExecuteType(executeType);
            scenarioExecutionInfoMapper.insert(executionInfo);
        }
    }

    public void insertExecutionInfoByScenarioIds(String scenarioIdJsonString, String status, String triggerMode, String projectId, String executeType) {
        try {
            List<String> scenarioIdList = JSON.parseArray(scenarioIdJsonString, String.class);
            for (String scenarioId : scenarioIdList) {
                this.insertExecutionInfo(scenarioId, status, triggerMode, projectId, executeType);
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


    public List<ScenarioExecutionInfo> selectByProjectIdIsNull() {
        ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
        example.createCriteria().andProjectIdIsNull();
        return scenarioExecutionInfoMapper.selectByExample(example);
    }

    public void updateProjectIdBySourceIdAndProjectIdIsNull(String projectId, String executeType, String apiId) {
        if (StringUtils.isNoneEmpty(projectId, executeType, apiId)) {
            ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
            example.createCriteria().andProjectIdIsNull().andSourceIdEqualTo(apiId);
            ScenarioExecutionInfo updateModel = new ScenarioExecutionInfo();
            updateModel.setProjectId(projectId);
            updateModel.setExecuteType(executeType);

            scenarioExecutionInfoMapper.updateByExampleSelective(updateModel, example);
        }
    }

    public void deleteByIds(List<String> deleteIdList) {
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
            example.createCriteria().andIdIn(deleteIdList);
            scenarioExecutionInfoMapper.deleteByExample(example);
        }
    }

    public long countExecuteTimesByProjectID(String projectId, String triggerMode) {
        ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
        ScenarioExecutionInfoExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        if (StringUtils.isNotEmpty(triggerMode)) {
            criteria.andTriggerModeEqualTo(triggerMode);
        }
        return scenarioExecutionInfoMapper.countByExample(example);
    }
}
