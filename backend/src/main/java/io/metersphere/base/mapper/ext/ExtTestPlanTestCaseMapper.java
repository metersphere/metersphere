package io.metersphere.base.mapper.ext;

import io.metersphere.dto.TestCaseReportStatusResultDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanTestCaseMapper {

    List<TestCaseReportStatusResultDTO> getReportMetric(@Param("planId") String planId);

    List<String> getExecutors(@Param("planId") String planId);
}
