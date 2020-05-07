package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCase;
import io.metersphere.controller.request.testcase.QueryTestCaseRequest;
import io.metersphere.controller.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.dto.TestCaseReportMetricDTO;
import io.metersphere.dto.TestCaseReportResultDTO;
import io.metersphere.dto.TestPlanCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanTestCaseMapper {

    List<TestCaseReportResultDTO> getReportMetric(@Param("planId") String planId);

    List<String> getExecutors(@Param("planId") String planId);
}
