package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.dto.*;
import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import io.metersphere.plan.request.function.QueryTestPlanCaseRequest;
import io.metersphere.plan.request.function.TestPlanFuncCaseConditions;
import io.metersphere.request.BaseQueryRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtTestPlanTestCaseMapper {
    List<TestCaseReportStatusResultDTO> getReportMetric(@Param("planId") String planId);

    List<String> getExecutors(@Param("planId") String planId);

    List<TestPlanCaseDTO> list(@Param("request") QueryTestPlanCaseRequest request);

    List<TestPlanCaseDTO> listByPlanId(@Param("request") QueryTestPlanCaseRequest request);

    List<TestPlanCaseDTO> listByNode(@Param("request") QueryTestPlanCaseRequest request);

    List<TestPlanCaseDTO> listByNodes(@Param("request") QueryTestPlanCaseRequest request);

    List<String> findRelateTestPlanId(@Param("userId") String userId, @Param("workspaceId") String workspaceId, @Param("projectId") String projectId);

    List<TestPlanCaseDTO> getRecentTestedTestCase(@Param("request") QueryTestPlanCaseRequest request);

    List<TestPlanCaseDTO> getPendingTestCases(@Param("request") QueryTestPlanCaseRequest request);

    List<String> getStatusByPlanId(String planId);

    int updateTestCaseStates(@Param("ids") List<String> ids, @Param("reportStatus") String reportStatus);

    TestPlanCaseDTO get(String testPlanTestCaseId);

    void deleteByTestCaseID(String id);

    List<String> getExecResultByPlanId(String planId);

    List<TestPlanCaseDTO> listForMinder(@Param("request") QueryTestPlanCaseRequest request);

    List<TestCaseTestDTO> listTestCaseTest(@Param("caseId") String caseId);

    List<String> selectIds(@Param("request") TestPlanFuncCaseConditions conditions);

    List<String> selectIdsByQuery(@Param("request") BaseQueryRequest query);

    void update(@Param("count") int count, @Param("id") String id, @Param("caseId") String caseId, @Param("issues") String issues);

    List<PlanReportCaseDTO> selectForPlanReport(String planId);

    List<TestPlanCaseDTO> getCasesByStatusList(@Param("planId") String planId, @Param("statusList") List<String> statusList);

    List<String> selectPlanIds();

    List<String> getIdsOrderByUpdateTime(@Param("planId") String planId);

    Long getPreOrder(@Param("planId") String planId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("planId") String planId, @Param("baseOrder") Long baseOrder);

    List<TestCaseNodeDTO> getTestPlanCountNodes(@Param("request") QueryTestPlanCaseRequest request);

    List<String> projectIdsByPlanId(@Param("planId") String planId);

    List<CountMapDTO> getExecResultMapByPlanId(@Param("planId") String planId);

    String selectCaseId(String id);

    List<String> getCaseIdsByIds(@Param("ids") List<String> ids);

    int updateDiffExecResultByTestPlanCaseIdList(@Param("ids") List<String> testPlanCaseIdList, @Param("execResult") String execResult);
    int updateTestCaseLastResultByIds(@Param("caseIds") List<String> caseIds, @Param("execResult") String execResult);
    //修改
    int updateDiffExecResultByTestCaseIdAndTestPlanId(@Param("testCaseId") String testCaseId, @Param("testPlanId") String testPlanId, @Param("execResult") String execResult);
    int updateTestCaseLastResult(@Param("testCaseId") String testCaseId, @Param("execResult") String execResult);

    List<TestPlanTestCase> selectByAutomationCaseIdAndTestPlanId(@Param("automationCaseId") String automationCaseId, @Param("test_plan_id") String testPlanId);

    List<ExecutedCaseInfoResult> findFailureCaseInTestPlanByProjectIDAndExecuteTimeAndLimitNumber(@Param("projectId") String projectId, @Param("versionId") String version, @Param("startTimestamp") long startTimestamp, @Param("limitNumber") int limitNumber);

    @Select("SELECT id FROM test_plan_test_case WHERE plan_id = #{planId} AND case_id = #{caseId}")
    List<String> selectIdByTestCaseIdAndTestPlanId(@Param("caseId") String caseId, @Param("planId") String planId);
}
