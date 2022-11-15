package io.metersphere.service.definition;

import io.metersphere.base.domain.ApiCaseExecutionInfo;
import io.metersphere.base.domain.ApiCaseExecutionInfoExample;
import io.metersphere.base.mapper.ApiCaseExecutionInfoMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
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
    public void insertExecutionInfo(String apiCaseId, String result, String triggerMode, String projectId, String executeType, String version) {
        if (StringUtils.isNotEmpty(apiCaseId) && StringUtils.isNotEmpty(result)) {
            ApiCaseExecutionInfo executionInfo = new ApiCaseExecutionInfo();
            executionInfo.setResult(result);
            executionInfo.setSourceId(apiCaseId);
            executionInfo.setId(UUID.randomUUID().toString());
            executionInfo.setCreateTime(System.currentTimeMillis());
            executionInfo.setTriggerMode(triggerMode);
            executionInfo.setProjectId(projectId);
            executionInfo.setExecuteType(executeType);
            executionInfo.setVersion(version);
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

    public long countExecutedTimesByProjectIdAndVersion(String projectId, String executeType, String version) {
        ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
        ApiCaseExecutionInfoExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId).andExecuteTypeEqualTo(executeType);
        if (StringUtils.isNotEmpty(version)) {
            criteria.andVersionEqualTo(version);
        }
        return apiCaseExecutionInfoMapper.countByExample(example);
    }

    public void updateProjectIdBySourceIdAndProjectIdIsNull(String projectId, String executeType, String version, String sourceId) {
        if (StringUtils.isNoneEmpty(projectId, executeType, sourceId)) {
            ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
            example.createCriteria().andProjectIdIsNull().andSourceIdEqualTo(sourceId);
            ApiCaseExecutionInfo updateModel = new ApiCaseExecutionInfo();
            updateModel.setProjectId(projectId);
            updateModel.setExecuteType(executeType);
            updateModel.setVersion(version);
            apiCaseExecutionInfoMapper.updateByExampleSelective(updateModel, example);
        }
    }

    public List<String> selectSourceIdByProjectIdIsNull() {
        return extApiTestCaseMapper.selectSourceIdByProjectIdIsNull();
    }

    public void deleteBySourceIdAndProjectIsNull(String sourceId) {
        ApiCaseExecutionInfoExample example = new ApiCaseExecutionInfoExample();
        example.createCriteria().andProjectIdIsNull().andSourceIdEqualTo(sourceId);
        apiCaseExecutionInfoMapper.deleteByExample(example);
    }

    public long countSourceIdByProjectIdIsNull() {
        return extApiTestCaseMapper.countSourceIdByProjectIdIsNull();
    }
}
