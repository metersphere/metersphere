package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.ApiTestCaseResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtApiTestCaseMapper {

    List<ApiTestCaseResult> list(@Param("request") ApiTestCaseRequest request);

    List<ApiTestCaseDTO> listSimple(@Param("request") ApiTestCaseRequest request);

    List<String> selectIdsNotExistsInPlan(@Param("projectId") String projectId, @Param("planId") String planId);

    @Select({
            "SELECT apiDef.protocol AS groupField,COUNT(testCase.id) AS countNumber FROM api_test_case testCase ",
            "INNER JOIN  api_definition apiDef ON testCase.api_definition_id = apiDef.id ",
            "WHERE testCase.project_id = #{0} ",
            "GROUP BY apiDef.protocol "
    })
    List<ApiDataCountResult> countProtocolByProjectID(String projectId);

    @Select({
            "SELECT count(testCase.id) AS countNumber FROM api_test_case testCase ",
            "INNER JOIN  api_definition apiDef ON testCase.api_definition_id = apiDef.id ",
            "WHERE testCase.project_id = #{projectId} ",
            "AND testCase.create_time BETWEEN #{firstDayTimestamp} AND #{lastDayTimestamp} "
    })
    long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);
}