package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.track.dto.PlanReportCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface ExtTestPlanScenarioCaseMapper {
    void insertIfNotExists(@Param("request") TestPlanApiScenario request);

    List<ApiScenarioDTO> list(@Param("request") TestPlanScenarioRequest request);

    List<String> getExecResultByPlanId(String planId);

    List<String> getIdsByPlanId(String planId);

    List<String> getNotRelevanceCaseIds(String planId, List<String> relevanceProjectIds);

    List<String> selectIds(@Param("request")TestPlanScenarioRequest request);

    List<TestPlanApiScenario> selectByIds(@Param("ids")String ids ,@Param("oderId")String oderId );

    List<TestPlanApiScenario> selectLegalDataByTestPlanId(String planId);

    List<PlanReportCaseDTO> selectForPlanReport(String planId);

    List<TestPlanFailureScenarioDTO> getFailureList(@Param("planId") String planId, @Param("status") String status);

    List<TestPlanFailureScenarioDTO> getFailureListByIds(@Param("ids") Collection<String> ids,@Param("status") String status);

    List<Integer> getUnderwaySteps(@Param("ids") List<String> underwayIds);

    String getProjectIdById(String testPlanScenarioId);

    List<String> selectPlanIds();

    List<String> getIdsOrderByUpdateTime(@Param("planId") String planId);

    Long getPreOrder(@Param("planId")String planId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("planId")String planId, @Param("baseOrder") Long baseOrder);
}
