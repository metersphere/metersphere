package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.base.domain.TestPlanUiScenario;
import io.metersphere.dto.TestPlanUiScenarioDTO;
import io.metersphere.dto.UiScenarioDTO;
import io.metersphere.track.dto.PlanReportCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface ExtTestPlanUiScenarioCaseMapper {
    void insertIfNotExists(@Param("request") TestPlanUiScenario request);

    List<UiScenarioDTO> list(@Param("request") TestPlanScenarioRequest request);

    List<String> getExecResultByPlanId(String planId);

    List<String> getIdsByPlanId(String planId);

    List<String> getNotRelevanceCaseIds(String planId, List<String> relevanceProjectIds);

    List<String> selectIds(@Param("request")TestPlanScenarioRequest request);

    List<TestPlanUiScenario> selectByIds(@Param("ids")String ids ,@Param("oderId")String oderId );

    List<TestPlanUiScenario> selectLegalDataByTestPlanId(String planId);

    List<PlanReportCaseDTO> selectForPlanReport(String planId);

    List<TestPlanUiScenarioDTO> getPlanUiScenarioByStatusList(@Param("planId") String planId, @Param("statusList") List<String> statusList);

    List<TestPlanUiScenarioDTO> getFailureListByIds(@Param("ids") Collection<String> ids, @Param("status") String status);

    List<Integer> getUnderwaySteps(@Param("ids") List<String> underwayIds);

    String getProjectIdById(String testPlanScenarioId);

    List<String> selectPlanIds();

    List<String> getIdsOrderByUpdateTime(@Param("planId") String planId);

    Long getPreOrder(@Param("planId")String planId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("planId")String planId, @Param("baseOrder") Long baseOrder);
}
