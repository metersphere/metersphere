package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.ApiComputeResult;
import io.metersphere.api.dto.definition.ApiDefinitionRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.ApiSwaggerUrlDTO;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionExample;
import io.metersphere.controller.request.BaseQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionMapper {
    List<ApiSwaggerUrlDTO> selectScheduleList(@Param("projectId") String projectId);

    List<ApiDefinitionResult> list(@Param("request") ApiDefinitionRequest request);

    List<ApiComputeResult> selectByIds(@Param("ids") List<String> ids);

    int removeToGc(@Param("ids") List<String> ids);

    int removeToGcByExample(ApiDefinitionExample example);

    int reduction(@Param("ids") List<String> ids);

    List<ApiDataCountResult> countProtocolByProjectID(String projectId);

    Long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    List<ApiDataCountResult> countStateByProjectID(String projectId);

    List<ApiDataCountResult> countApiCoverageByProjectID(String projectId);

    ApiDefinition getNextNum(@Param("projectId") String projectId);

    List<ApiDefinitionResult> listRelevance(@Param("request")ApiDefinitionRequest request);

    List<String> selectIds(@Param("request") BaseQueryRequest query);
}