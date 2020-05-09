package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCase;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.controller.request.testcase.QueryTestCaseRequest;
import io.metersphere.controller.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.dto.ReportDTO;
import io.metersphere.dto.TestCaseDTO;
import io.metersphere.dto.TestPlanCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseMapper {

    List<TestCase> getTestCaseNames(@Param("request") QueryTestCaseRequest request);

    List<TestPlanCaseDTO> getTestPlanTestCases(@Param("request") QueryTestPlanCaseRequest request);

    List<TestCaseDTO> list(@Param("request") QueryTestCaseRequest request);
}
