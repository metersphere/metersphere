package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCase;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.track.request.testplancase.TestPlanFuncCaseConditions;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 根据项目 ids 查询 TestPlanCaseDTO 列表
     *
     * @param ids project id list
     * @return List<TestPlanCaseDTO>
     */
    List<TestPlanCaseDTO> listTestCaseByProjectIds(@Param("ids") List<String> ids);

    TestPlanCaseDTO get(String testPlanTestCaseId);

    void deleteByTestCaseID(String id);

    List<String> getExecResultByPlanId(String planId);

    List<TestPlanCaseDTO> listForMinder(@Param("request") QueryTestPlanCaseRequest request);

    List<TestCaseTestDTO> listTestCaseTest(@Param("caseId") String caseId);

    List<String> selectIds(@Param("request") TestPlanFuncCaseConditions conditions);

    List<String> selectIdsByQuery(@Param("request") BaseQueryRequest query);

    void update(@Param("count") int count, @Param("id") String id, @Param("caseId") String caseId, @Param("issues") String issues);

    List<PlanReportCaseDTO> selectForPlanReport(String planId);

    List<TestPlanCaseDTO> getCases(@Param("planId") String planId, @Param("status") String status);

    List<String> selectPlanIds();

    List<String> getIdsOrderByUpdateTime(@Param("planId") String planId);

    Long getPreOrder(@Param("planId") String planId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("planId") String planId, @Param("baseOrder") Long baseOrder);

    List<TestCase> getTestCaseWithNodeInfo(@Param("planId") String planId);

    List<CountMapDTO> getExecResultMapByPlanId(@Param("planId") String planId);
}
