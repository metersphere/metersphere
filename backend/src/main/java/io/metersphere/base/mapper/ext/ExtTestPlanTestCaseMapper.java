package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.TestCaseReportStatusResultDTO;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanTestCaseMapper {

    List<TestCaseReportStatusResultDTO> getReportMetric(@Param("planId") String planId);

    List<String> getExecutors(@Param("planId") String planId);

    List<TestPlanCaseDTO> list(@Param("request") QueryTestPlanCaseRequest request);

    List<TestPlanCaseDTO> listByNode(@Param("request") QueryTestPlanCaseRequest request);

    List<TestPlanCaseDTO> listByNodes(@Param("request") QueryTestPlanCaseRequest request);

    List<String> findRelateTestPlanId(@Param("userId") String userId, @Param("workspaceId") String workspaceId);

    List<TestPlanCaseDTO> getRecentTestedTestCase(@Param("request") QueryTestPlanCaseRequest request);

    List<TestPlanCaseDTO> getPendingTestCases(@Param("request") QueryTestPlanCaseRequest request);

    List<String> getStatusByPlanId(String planId);

    int updateTestCaseStates(@Param("ids") List<String> ids, @Param("reportStatus") String reportStatus);

    List<String> getTestPlanTestCaseIds(String testId);

    /**
     * 根据项目 ids 查询 TestPlanCaseDTO 列表
     * @param ids project id list
     * @return List<TestPlanCaseDTO>
     */
    List<TestPlanCaseDTO> listTestCaseByProjectIds(@Param("ids") List<String> ids);

    TestPlanCaseDTO get(String testPlanTestCaseId);
    /**
     * 获取完整的测试计划下用例的详细信息
     * @param request id(test_plan_test_case.id) 不能为空
     * @return TestPlanCaseDTO
     */
    TestPlanCaseDTO getTestPlanTestCase(@Param("request") QueryTestPlanCaseRequest request);

    /**
     * 获取测试计划下的 TestPlanTestCaseID 和 TestCaseName
     * @param request planId 不能为空
     * @return List<TestPlanCaseDTO>
     */
    List<TestPlanCaseDTO> getTestPlanTestCaseList(@Param("request") QueryTestPlanCaseRequest request);

}
