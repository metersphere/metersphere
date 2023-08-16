package io.metersphere.base.mapper.plan.ext;

import io.metersphere.api.dto.QueryReferenceRequest;
import io.metersphere.api.dto.automation.TestPlanApiDTO;
import io.metersphere.api.dto.automation.TestPlanDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.dto.plan.TestPlanApiCaseInfoDTO;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ExtTestPlanApiCaseMapper {
    void insertIfNotExists(@Param("request") TestPlanApiCase request);

    List<TestPlanApiCaseDTO> list(@Param("request") ApiTestCaseRequest request);

    List<String> getExecResultByPlanId(String planId);

    List<String> getIdsByPlanId(String planId);

    List<String> getNotRelevanceCaseIds(@Param("planId") String planId, @Param("relevanceProjectIds") List<String> relevanceProjectIds);

    List<String> getStatusByTestPlanId(String id);

    List<String> selectIds(@Param("request") ApiTestCaseRequest request);

    ApiTestCaseWithBLOBs getApiTestCaseById(String testPlanApiCaseId);

    String getApiTestCaseIdById(String testPlanApiCaseId);

    List<TestPlanApiCaseInfoDTO> selectLegalDataByTestPlanId(String planId);

    List<TestPlanApiCaseInfoDTO> selectByPlanCaseIds(List<String> planCaseIds);

    List<Map> selectForPlanReport(String planId);

    List<TestPlanApiDTO> getFailureList(@Param("planId") String planId, @Param("status") String status);

    List<TestPlanApiDTO> getFailureListByIds(@Param("ids") Collection<String> caseIdList, @Param("status") String status);

    List<String> selectPlanIds();

    List<String> getIdsOrderByUpdateTime(@Param("planId") String planId);

    Long getPreOrder(@Param("planId") String planId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("planId") String planId, @Param("baseOrder") Long baseOrder);

    List<TestPlanDTO> selectTestPlanByRelevancy(@Param("request") QueryReferenceRequest params);

    List<TestPlanApiCase> selectPlanByIdsAndStatusIsNotTrash(@Param("ids") List<String> ids);

    List<TestPlanApiCase> selectByIdsAndStatusIsNotTrash(@Param("ids") List<String> ids);

    List<String> selectNameByIdIn(@Param("ids") List<String> ids);

    String selectProjectId(String id);

    List<TestPlanApiCase> selectByRefIds(@Param("ids") List<String> ids);

    List<String> selectResourcePoolIdByReportIds(@Param("ids") List<String> resourceIds);

    void updateStatusStop(@Param("ids") List<String> testIds);

    List<String> getCaseProjectIdByPlanId(String planId);
}

