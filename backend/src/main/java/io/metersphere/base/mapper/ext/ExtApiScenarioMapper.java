package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtApiScenarioMapper {
    List<ApiScenarioDTO> list(@Param("request") ApiScenarioRequest request);

    List<ApiScenarioWithBLOBs> selectByTagId(@Param("id") String id);

    List<ApiScenarioWithBLOBs> selectIds(@Param("ids") List<String> ids);

    List<ApiScenario> selectReference(@Param("request") ApiScenarioRequest request);

    int removeToGc(@Param("ids") List<String> ids);

    int reduction(@Param("ids") List<String> ids);

    @Select("SELECT COUNT(id) AS countNumber FROM api_scenario WHERE project_id = #{0} ")
    long countByProjectID(String projectId);

    @Select({
            "SELECT count(id) AS countNumber FROM api_scenario ",
            "WHERE project_id = #{projectId} ",
            "AND create_time BETWEEN #{firstDayTimestamp} AND #{lastDayTimestamp} "
    })
    long countByProjectIDAndCreatInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);
}
