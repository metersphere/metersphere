package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.ApiCountChartResult;
import io.metersphere.api.dto.ApiCountRequest;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.response.ExecuteResultCountDTO;
import io.metersphere.api.dto.definition.*;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.dto.BaseCase;
import io.metersphere.dto.ParamsDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiTestCaseMapper {

    List<ApiTestCaseResult> list(@Param("request") ApiTestCaseRequest request);

    List<ApiTestCaseDTO> listSimple(@Param("request") ApiTestCaseRequest request);

    List<String> selectIdsNotExistsInPlan(@Param("projectId") String projectId, @Param("planId") String planId);

    List<String> selectIdsNotExistsInReview(@Param("projectId") String projectId, @Param("reviewId") String reviewId);

    List<ApiDataCountResult> countProtocolByProjectID(String projectId);

    long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    List<ApiTestCaseInfo> getRequest(@Param("request") ApiTestCaseRequest request);

    ApiTestCase getNextNum(@Param("definitionId") String definitionId);

    ApiTestCaseInfo selectApiCaseInfoByPrimaryKey(String id);

    List<ApiTestCase> selectEffectiveTestCaseByProjectId(String projectId);

    List<String> idSimple(@Param("request") ApiTestCaseRequest request);

    List<ApiTestCaseInfo> getCaseInfo(@Param("request") ApiTestCaseRequest request);

    ApiDefinition findApiUrlAndMethodById(String id);

    int deleteToGc(ApiTestCaseRequest request);

    int reduction(@Param("ids") List<String> ids);

    List<ApiTestCaseDTO> getCannotReductionApiCaseList(@Param("ids") List<String> ids);

    List<String> selectCaseIdsByApiIds(@Param("ids") List<String> apiIds);

    List<String> selectNameByIdIn(@Param("ids") List<String> ids);

    String selectNameById(String id);

    List<String> selectIdsByQuery(@Param("request") ApiTestCaseRequest request);

    List<String> selectProjectIds();

    List<String> getIdsOrderByUpdateTime(@Param("projectId") String projectId);

    Long getPreOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    /**
     * 获取接口用例的环境
     *
     * @param caseId 用例ID
     * @return ApiEnvironment
     */
    String getApiCaseEnvironment(@Param("caseId") String caseId);

    int moduleCount(@Param("request") ApiTestCaseRequest request);

    List<ParamsDTO> findPassRateByIds(@Param("ids") List<String> ids);

    List<ParamsDTO> getApiCaseEnvironments(@Param("caseIds") List<String> caseIds);

    void insertNewVersionCases(@Param("api") ApiDefinition apiDefinition, @Param("old") ApiDefinition old);

    List<ApiTestCase> checkName(@Param("request") SaveApiTestCaseRequest request);

    int toBeUpdateCase(@Param("ids") List<String> ids, @Param("toBeUpdate") Boolean toBeUpdate);

    int countById(String resourceID);

    List<ExecuteResultCountDTO> selectExecuteResultByProjectId(String projectId);

    int deleteCaseToGc(ApiTestCaseRequest request);

    List<ApiTestCaseWithBLOBs> unTrashCaseListByIds(@Param("ids") List<String> ids);

    List<ApiCountChartResult> countByRequest(ApiCountRequest request);

    List<ApiTestCaseDTO> relevanceApiList(@Param("request") ApiTestCaseRequest request);

    List<BaseCase> selectBaseCaseByProjectId(@Param("projectId") String projectId);

    int getCaseCountById(String id);
}
