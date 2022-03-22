package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiTestCase;
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
}
