package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanReportApiCase;
import io.metersphere.plan.domain.TestPlanReportApiScenario;
import io.metersphere.plan.mapper.ExtTestPlanReportCaseMapper;
import io.metersphere.plan.mapper.TestPlanReportApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanReportApiScenarioMapper;
import io.metersphere.plan.mapper.TestPlanReportFunctionCaseMapper;
import io.metersphere.sdk.constants.ResultStatus;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanReportCaseService {
    @Resource
    private TestPlanReportFunctionCaseMapper testPlanReportFunctionCaseMapper;

    @Resource
    private TestPlanReportApiCaseMapper testPlanReportApiCaseMapper;

    @Resource
    private TestPlanReportApiScenarioMapper testPlanReportApiScenarioMapper;

    @Resource
    private ExtTestPlanReportCaseMapper extTestPlanReportCaseMapper;

    public List<String> selectFunctionalCaseIdsByTestPlanId(String testPlanId) {
        return extTestPlanReportCaseMapper.selectFunctionalCaseIdsByTestPlanId(testPlanId);
    }

    public Map<String, String> selectCaseExecResultByReportId(String testPlanReportId) {
        List<TestPlanReportApiCase> apiCases = extTestPlanReportCaseMapper.selectApiCaseIdAndResultByReportId(testPlanReportId);
        List<TestPlanReportApiScenario> scenarios = extTestPlanReportCaseMapper.selectApiScenarioIdAndResultByReportId(testPlanReportId);
        Map<String, String> returnMap = new HashMap<>();
        apiCases.forEach(item -> returnMap.put(item.getApiCaseId(), item.getApiCaseExecuteResult()));
        scenarios.forEach(item -> returnMap.put(item.getApiScenarioId(), item.getApiScenarioExecuteResult()));
        return returnMap;
    }

    /*
    1.只要有失败的就算失败
    2.没有失败的情况下，有未执行的就不更新
    3.1和2都不满足的情况下就是成功
     */
    public String calculateFuncCaseExecResult(List<String> params) {
        if (CollectionUtils.isNotEmpty(params)) {
            List<String> execResult = new ArrayList<>(params.stream().distinct().toList());
            if (execResult.contains(ResultStatus.ERROR.name())) {
                return ResultStatus.ERROR.name();
            } else {
                execResult.remove(ResultStatus.SUCCESS.name());
                execResult.remove(ResultStatus.FAKE_ERROR.name());
                if (execResult.size() > 0) {
                    return null;
                } else {
                    return ResultStatus.SUCCESS.name();
                }
            }
        }
        return null;
    }
}
