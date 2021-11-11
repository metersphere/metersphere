package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.track.dto.PlanReportCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface ExtTestPlanApiCaseMapper {
    void insertIfNotExists(@Param("request") TestPlanApiCase request);

    List<TestPlanApiCaseDTO> list(@Param("request") ApiTestCaseRequest request);

    List<String> getExecResultByPlanId(String planId);

    List<String> getIdsByPlanId(String planId);

    List<String> getNotRelevanceCaseIds(@Param("planId")String planId, @Param("relevanceProjectIds")List<String> relevanceProjectIds);

    List<String> getStatusByTestPlanId(String id);

    List<String> selectIds(@Param("request") ApiTestCaseRequest request);

    ApiTestCaseWithBLOBs getApiTestCaseById(String testPlanApiCaseId);


    List<TestPlanApiCase> selectLegalDataByTestPlanId(String planId);

    List<PlanReportCaseDTO> selectForPlanReport(String planId);

    List<TestPlanFailureApiDTO> getFailureList(@Param("planId") String planId, @Param("status") String status);

    List<TestPlanFailureApiDTO> getFailureListByIds(@Param("ids") Collection<String> caseIdList,@Param("status") String status);

    List<String> selectPlanIds();

    List<String> getIdsOrderByUpdateTime(@Param("planId") String planId);

    Long getPreOrder(@Param("planId")String planId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("planId")String planId, @Param("baseOrder") Long baseOrder);
}

