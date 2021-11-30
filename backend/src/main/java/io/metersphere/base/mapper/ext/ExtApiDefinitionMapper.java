package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.ApiComputeResult;
import io.metersphere.api.dto.definition.ApiDefinitionRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.ApiSwaggerUrlDTO;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionExample;
import io.metersphere.base.domain.ApiDefinitionExampleWithOperation;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.dto.RelationshipGraphData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExtApiDefinitionMapper {
    List<ApiSwaggerUrlDTO> selectScheduleList(@Param("projectId") String projectId);

    List<ApiDefinitionResult> list(@Param("request") ApiDefinitionRequest request);

    List<ApiDefinitionResult> weekList(@Param("request") ApiDefinitionRequest request , @Param("startTimestamp") long startTimestamp );

    List<Scenario> scenarioList(@Param("apiDefinitionId") String apiDefinitionId);

    int moduleCount(@Param("request") ApiDefinitionRequest request);

    //List<ApiComputeResult> selectByIds(@Param("ids") List<String> ids);

    List<ApiComputeResult> selectByIds(@Param("ids") List<String> ids, @Param("projectId") String projectId);

    List<ApiComputeResult> selectByIdsAndStatusIsNotTrash(@Param("ids") List<String> ids, @Param("projectId") String projectId);

//    int removeToGc(@Param("ids") List<String> ids);

    int removeToGcByExample(ApiDefinitionExampleWithOperation example);

    int reduction(@Param("ids") List<String> ids);

    List<ApiDataCountResult> countProtocolByProjectID(String projectId);

    Long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    List<ApiDataCountResult> countStateByProjectID(String projectId);

    List<ApiDataCountResult> countApiCoverageByProjectID(String projectId);

    ApiDefinition getNextNum(@Param("projectId") String projectId);

    List<ApiDefinitionResult> listRelevance(@Param("request") ApiDefinitionRequest request);

    List<ApiDefinitionResult> listRelevanceReview(@Param("request") ApiDefinitionRequest request);

    List<String> selectIds(@Param("request") BaseQueryRequest query);

    List<ApiDefinition> selectEffectiveIdByProjectId(String projectId);

    List<ApiDefinitionResult> listByIds(@Param("ids") List<String> ids);

    List<Map<String, Object>> moduleCountByCollection(@Param("request") ApiDefinitionRequest request);

    ApiDefinition selectUrlAndMethodById(String id);

    int checkOriginalStatusByIds(@Param("ids")List<String> ids);

    List<String> selectProjectIds();

    List<String> getIdsOrderByUpdateTime(@Param("projectId") String projectId);

    Long getPreOrder(@Param("projectId")String projectId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("projectId")String projectId, @Param("baseOrder") Long baseOrder);

    long countQuotedApiByProjectId(String projectId);

    List<RelationshipGraphData.Node> getForGraph(@Param("ids") Set<String> ids);

    int countByIds(@Param("ids") List<String> ids);

    long countByExample(ApiDefinitionExample example);
}
