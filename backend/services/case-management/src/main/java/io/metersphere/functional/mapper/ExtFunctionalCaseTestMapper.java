package io.metersphere.functional.mapper;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.functional.dto.FunctionalCaseTestDTO;
import io.metersphere.functional.dto.FunctionalCaseTestPlanDTO;
import io.metersphere.functional.dto.TestPlanCaseExecuteHistoryDTO;
import io.metersphere.functional.request.AssociatePlanPageRequest;
import io.metersphere.functional.request.DisassociateOtherCaseRequest;
import io.metersphere.functional.request.FunctionalCaseTestRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFunctionalCaseTestMapper {

    List<String> getIds(@Param("request") DisassociateOtherCaseRequest request);

    Integer getOtherCaseCount(@Param("caseId") String caseId);

    List<FunctionalCaseTestDTO> getList(@Param("request") FunctionalCaseTestRequest request);

    List<FunctionalCaseTestPlanDTO> getPlanList(@Param("request") AssociatePlanPageRequest request);

    List<TestPlanCaseExecuteHistoryDTO> getPlanExecuteHistoryList(@Param("caseId") String caseId, @Param("planId") String planId);

    List<ApiTestCase> selectApiCaseByCaseIds(@Param("caseIds") List<String> caseIds);

    List<ApiScenario> selectApiScenarioByCaseIds(@Param("caseIds") List<String> caseIds);
}
