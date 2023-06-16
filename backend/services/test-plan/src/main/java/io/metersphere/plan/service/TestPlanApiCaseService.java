package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.domain.TestPlanApiCaseExample;
import io.metersphere.plan.dto.TestPlanApiCaseDTO;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
import io.metersphere.sdk.util.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseService {

    @Resource
    TestPlanApiCaseMapper testPlanApiCaseMapper;

    public int deleteByTestPlanId(String testPlanId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public int deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        return testPlanApiCaseMapper.deleteByExample(example);
    }
}
