package io.metersphere.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.controller.request.testcase.QueryTestCaseRequest;
import io.metersphere.controller.request.testcase.QueryTestPlanRequest;
import io.metersphere.dto.TestPlanDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService {

    @Resource
    TestPlanMapper testPlanMapper;

    @Resource
    ExtTestPlanMapper extTestPlanMapper;

    public void addTestPlan(TestPlan testPlan) {
        testPlan.setId(UUID.randomUUID().toString());
        testPlan.setStatus(TestPlanStatus.Prepare.name());
        testPlan.setCreateTime(System.currentTimeMillis());
        testPlan.setUpdateTime(System.currentTimeMillis());
        testPlanMapper.insert(testPlan);
    }


    public List<TestPlan> getTestPlan(String testPlanId) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andIdEqualTo(testPlanId);
        return testPlanMapper.selectByExampleWithBLOBs(testPlanExample);
    }

    public int editTestPlan(TestPlan testPlan) {
        testPlan.setUpdateTime(System.currentTimeMillis());
        return testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }

    public int deleteTestPlan(String testPalnId) {
        return testPlanMapper.deleteByPrimaryKey(testPalnId);
    }

    public List<TestPlanDTO> listTestPlan(QueryTestPlanRequest request) {
        return extTestPlanMapper.list(request);
    }
}
