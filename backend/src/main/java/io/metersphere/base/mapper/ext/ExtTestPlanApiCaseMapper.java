package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import org.apache.ibatis.annotations.Param;

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
}