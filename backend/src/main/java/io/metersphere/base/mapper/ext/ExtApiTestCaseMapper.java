package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.dataCount.ApiDataCountResult;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.ApiTestCaseResult;
import io.metersphere.base.domain.ApiTestCase;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtApiTestCaseMapper {

    List<ApiTestCaseResult> list(@Param("request") ApiTestCaseRequest request);
    List<ApiTestCaseDTO> listSimple(@Param("request") ApiTestCaseRequest request);

    List<ApiDataCountResult> countProtocolByProjectID(String projectId);

    long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);
}