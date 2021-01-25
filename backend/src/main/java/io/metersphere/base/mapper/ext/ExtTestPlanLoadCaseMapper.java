package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanLoadCaseMapper {

    List<String> selectIdsNotInPlan(@Param("projectId") String projectId, @Param("planId") String planId);
    List<TestPlanLoadCaseDTO> selectTestPlanLoadCaseList(@Param("request") LoadCaseRequest request);
    void updateCaseStatus(@Param("reportId") String reportId, @Param("status") String status);
    List<String> getStatusByTestPlanId(@Param("planId") String planId);
}
