package io.metersphere.base.mapper;

import io.metersphere.api.dto.dataCount.ApiDataCountResult;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.ApiTestCaseExample;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ApiTestCaseMapper {
    long countByExample(ApiTestCaseExample example);

    int deleteByExample(ApiTestCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTestCaseWithBLOBs record);

    int insertSelective(ApiTestCaseWithBLOBs record);

    List<ApiTestCaseWithBLOBs> selectByExampleWithBLOBs(ApiTestCaseExample example);

    List<ApiTestCase> selectByExample(ApiTestCaseExample example);

    ApiTestCaseWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTestCaseWithBLOBs record, @Param("example") ApiTestCaseExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiTestCaseWithBLOBs record, @Param("example") ApiTestCaseExample example);

    int updateByExample(@Param("record") ApiTestCase record, @Param("example") ApiTestCaseExample example);

    int updateByPrimaryKeySelective(ApiTestCaseWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiTestCaseWithBLOBs record);

    int updateByPrimaryKey(ApiTestCase record);

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