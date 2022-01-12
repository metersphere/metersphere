package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.PlanReportCaseDTO;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface ExtTestPlanLoadCaseMapper {

    List<String> selectIdsNotInPlan(@Param("request") LoadCaseRequest request);

    List<TestPlanLoadCaseDTO> selectTestPlanLoadCaseList(@Param("request") LoadCaseRequest request);

    List<TestPlanLoadCaseDTO> selectByIdIn(@Param("request") LoadCaseRequest request);

    void updateCaseStatus(@Param("reportId") String reportId, @Param("status") String status);

    void updateCaseStatusByApi(@Param("testPlanId") String testPlanId, @Param("loadCaseId") String loadCaseId, @Param("status") String status);

    List<String> getStatusByTestPlanId(@Param("planId") String planId);

    List<String> selectTestPlanLoadCaseId(@Param("request") LoadCaseRequest request);

    List<PlanReportCaseDTO> selectForPlanReport(String planId);

    List<TestPlanLoadCaseDTO> getCases(@Param("planId") String planId, @Param("status") String status);

    List<TestPlanLoadCaseDTO> getCasesByIds(@Param("ids") Collection<String> ids, @Param("reportIds") Collection<String> reportIds);

    List<String> selectPlanIds();

    List<String> getIdsOrderByUpdateTime(@Param("planId") String planId);

    Long getPreOrder(@Param("planId") String planId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("planId") String planId, @Param("baseOrder") Long baseOrder);

    List<String> selectIdByLoadCaseReportIdAndStatusIsRun(String reportId);

    void updateStatusNullById(String id);
}
