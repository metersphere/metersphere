package io.metersphere.service;

import io.metersphere.base.domain.FunctionCaseExecutionInfo;
import io.metersphere.base.domain.FunctionCaseExecutionInfoExample;
import io.metersphere.base.mapper.FunctionCaseExecutionInfoMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FunctionCaseExecutionInfoService {
    @Resource
    private FunctionCaseExecutionInfoMapper functionCaseExecutionInfoMapper;

    public void insertExecutionInfo(String caseId, String result) {
        if (!StringUtils.isAnyEmpty(caseId, result)) {
            FunctionCaseExecutionInfo executionInfo = new FunctionCaseExecutionInfo();
            executionInfo.setResult(result);
            executionInfo.setSourceId(caseId);
            executionInfo.setId(UUID.randomUUID().toString());
            executionInfo.setCreateTime(System.currentTimeMillis());
            functionCaseExecutionInfoMapper.insert(executionInfo);
        }
    }

    public void deleteBySourceIdList(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            FunctionCaseExecutionInfoExample example = new FunctionCaseExecutionInfoExample();
            example.createCriteria().andSourceIdIn(ids);
            functionCaseExecutionInfoMapper.deleteByExample(example);
        }
    }

    public void deleteBySourceId(String id) {
        if (StringUtils.isNotEmpty(id)) {
            FunctionCaseExecutionInfoExample example = new FunctionCaseExecutionInfoExample();
            example.createCriteria().andSourceIdEqualTo(id);
            functionCaseExecutionInfoMapper.deleteByExample(example);
        }
    }
}
