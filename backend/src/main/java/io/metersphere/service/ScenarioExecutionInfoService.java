package io.metersphere.service;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.base.domain.ScenarioExecutionInfo;
import io.metersphere.base.domain.ScenarioExecutionInfoExample;
import io.metersphere.base.mapper.ScenarioExecutionInfoMapper;
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
public class ScenarioExecutionInfoService {
    @Resource
    private ScenarioExecutionInfoMapper scenarioExecutionInfoMapper;

    @Lazy
    public void insertExecutionInfo(String scenarioId, String result, String triggerMode) {
        if (StringUtils.isNotEmpty(scenarioId) && StringUtils.isNotEmpty(result)) {
            ScenarioExecutionInfo executionInfo = new ScenarioExecutionInfo();
            executionInfo.setResult(result);
            executionInfo.setSourceId(scenarioId);
            executionInfo.setId(UUID.randomUUID().toString());
            executionInfo.setCreateTime(System.currentTimeMillis());
            executionInfo.setTriggerMode(triggerMode);
            scenarioExecutionInfoMapper.insert(executionInfo);
        }
    }

    public void insertExecutionInfoByScenarioIds(String scenarioIdJsonString, String status, String triggerMode) {
        try {
            List<String> scenarioIdList = JSONArray.parseArray(scenarioIdJsonString, String.class);
            for (String scenarioId : scenarioIdList) {
                this.insertExecutionInfo(scenarioId, status, triggerMode);
            }
        } catch (Exception e) {
            LogUtil.error("解析场景ID的JSON" + scenarioIdJsonString + "失败！", e);
        }
    }

    public void deleteByScenarioIdList(List<String> resourceIdList) {
        if (CollectionUtils.isNotEmpty(resourceIdList)) {
            ScenarioExecutionInfoExample example = new ScenarioExecutionInfoExample();
            example.createCriteria().andSourceIdIn(resourceIdList);
            scenarioExecutionInfoMapper.deleteByExample(example);
        }
    }
}
