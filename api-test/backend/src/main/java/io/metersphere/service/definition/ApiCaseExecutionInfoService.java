package io.metersphere.service.definition;

import io.metersphere.base.domain.ApiCaseExecutionInfo;
import io.metersphere.base.domain.ApiCaseExecutionInfoExample;
import io.metersphere.base.mapper.ApiCaseExecutionInfoMapper;
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

    @Lazy
    public void insertExecutionInfo(String apiCaseId, String result, String triggerMode, String projectId, String executeType) {
        if (StringUtils.isNotEmpty(apiCaseId) && StringUtils.isNotEmpty(result)) {
            ApiCaseExecutionInfo executionInfo = new ApiCaseExecutionInfo();
            executionInfo.setResult(result);
            executionInfo.setSourceId(apiCaseId);
            executionInfo.setId(UUID.randomUUID().toString());
            executionInfo.setCreateTime(System.currentTimeMillis());
            executionInfo.setTriggerMode(triggerMode);
            executionInfo.setProjectId(projectId);
            executionInfo.setExecuteType(executeType);
            apiCaseExecutionInfoMapper.insert(executionInfo);
        }
    }


    public void deleteByProjectId(String projectId) {
        if (StringUtils.isNotEmpty(projectId)) {
            ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
            example.createCriteria().andProjectIdEqualTo(projectId);
            apiCaseExecutionInfoMapper.deleteByExample(example);
        }
    }

    public long countExecutedTimesByProjectId(String projectId, String executeType) {
        ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andExecuteTypeEqualTo(executeType);
        return apiCaseExecutionInfoMapper.countByExample(example);
    }


    public List<ApiCaseExecutionInfo> selectByProjectIdIsNull() {
        ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
        example.createCriteria().andProjectIdIsNull();
        return apiCaseExecutionInfoMapper.selectByExample(example);
    }

    public void updateProjectIdBySourceIdAndProjectIdIsNull(String projectId, String executeType, String apiId) {
        if (StringUtils.isNoneEmpty(projectId, executeType, apiId)) {
            ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
            example.createCriteria().andProjectIdIsNull().andSourceIdEqualTo(apiId);
            ApiCaseExecutionInfo updateModel = new ApiCaseExecutionInfo();
            updateModel.setProjectId(projectId);
            updateModel.setExecuteType(executeType);

            apiCaseExecutionInfoMapper.updateByExampleSelective(updateModel, example);
        }
    }

    public void deleteByIds(List<String> deleteIdList) {
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
            example.createCriteria().andIdIn(deleteIdList);
            apiCaseExecutionInfoMapper.deleteByExample(example);
        }
    }
}
