package io.metersphere.base.mapper.ext;

import io.metersphere.controller.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.dto.TestCaseReportStatusResultDTO;
import io.metersphere.dto.TestPlanCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanTestCaseMapper {

    List<TestCaseReportStatusResultDTO> getReportMetric(@Param("planId") String planId);

    List<String> getExecutors(@Param("planId") String planId);

    List<TestPlanCaseDTO> list(@Param("request") QueryTestPlanCaseRequest request);

}
