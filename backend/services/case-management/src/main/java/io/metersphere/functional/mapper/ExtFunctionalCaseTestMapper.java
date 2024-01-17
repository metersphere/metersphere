package io.metersphere.functional.mapper;

import io.metersphere.functional.dto.FunctionalCaseTestDTO;
import io.metersphere.functional.dto.FunctionalCaseTestPlanDTO;
import io.metersphere.functional.request.AssociatePlanPageRequest;
import io.metersphere.functional.request.DisassociateOtherCaseRequest;
import io.metersphere.functional.request.FunctionalCaseTestRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFunctionalCaseTestMapper {

    List<String> getIds(@Param("request") DisassociateOtherCaseRequest request);

    List<FunctionalCaseTestDTO> getList(@Param("request") FunctionalCaseTestRequest request);

    List<FunctionalCaseTestPlanDTO> getPlanList(@Param("request") AssociatePlanPageRequest request);

}
