package io.metersphere.service;

import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.controller.request.testcase.QueryTestCaseRequest;
import io.metersphere.dto.TestPlanCaseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanTestCaseService {

    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;

    @Resource
    ExtTestCaseMapper extTestCaseMapper;

    public List<TestPlanCaseDTO> getTestPlanCases(QueryTestCaseRequest request) {
        return extTestCaseMapper.getTestPlanTestCases(request);
    }


    public void editTestCase(TestPlanTestCase testPlanTestCase) {
        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
        testPlanTestCaseMapper.updateByPrimaryKeySelective(testPlanTestCase);
    }

    public int deleteTestCase(Integer id) {
        return testPlanTestCaseMapper.deleteByPrimaryKey(id);
    }
}
