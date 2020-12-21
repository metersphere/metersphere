package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtApiScenarioReportMapper {
    List<APIScenarioReportResult> list(@Param("request") QueryAPIReportRequest request);

    APIScenarioReportResult get(@Param("reportId") String reportId);

    @Select("SELECT count(id) AS countNumber FROM api_scenario_report WHERE project_id = #{0} ")
    long countByProjectID(String projectId);

    @Select({
            "SELECT count(id) AS countNumber FROM api_scenario_report ",
            "WHERE project_id = #{projectId} ",
            "AND create_time BETWEEN #{firstDayTimestamp} AND #{lastDayTimestamp} "
    })
    long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

}