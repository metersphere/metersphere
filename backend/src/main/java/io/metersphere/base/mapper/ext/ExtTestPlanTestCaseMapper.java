package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Workspace;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.track.dto.TestCaseReportStatusResultDTO;
import io.metersphere.track.dto.TestPlanCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanTestCaseMapper {

    List<TestCaseReportStatusResultDTO> getReportMetric(@Param("planId") String planId);

    List<String> getExecutors(@Param("planId") String planId);

    List<TestPlanCaseDTO> list(@Param("request") QueryTestPlanCaseRequest request);

    List<TestPlanCaseDTO> listByNode(@Param("request") QueryTestPlanCaseRequest request);

    List<String> findRelateTestPlanId(@Param("userId") String userId, @Param("workspaceId") String workspaceId);

    List<TestPlanCaseDTO> getRecentTestedTestCase(@Param("request") QueryTestPlanCaseRequest request);

    List<TestPlanCaseDTO> getPendingTestCases(@Param("request") QueryTestPlanCaseRequest request);

    List<String> getStatusByPlanId(String planId);
}
