package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.dataCount.ApiDataCountResult;
import io.metersphere.api.dto.definition.ApiComputeResult;
import io.metersphere.api.dto.definition.ApiDefinitionRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtApiDefinitionMapper {

    List<ApiDefinitionResult> list(@Param("request") ApiDefinitionRequest request);

    List<ApiComputeResult> selectByIds(@Param("ids") List<String> ids);

    int removeToGc(@Param("ids") List<String> ids);

    int reduction(@Param("ids") List<String> ids);

    @Select("SELECT protocol AS groupField,count(id) AS countNumber FROM api_definition  WHERE project_id = #{0} GROUP BY protocol;")
    List<ApiDataCountResult> countProtocolByProjectID(String projectId);

    @Select({
            "SELECT count(id) AS countNumber FROM api_definition ",
            "WHERE project_id = #{projectId} ",
            "AND create_time BETWEEN #{firstDayTimestamp} AND #{lastDayTimestamp} "
    })
    long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);
}