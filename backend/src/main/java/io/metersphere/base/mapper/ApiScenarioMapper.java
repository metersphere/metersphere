package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ApiScenarioMapper {
    long countByExample(ApiScenarioExample example);

    int deleteByExample(ApiScenarioExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenario record);

    int insertSelective(ApiScenario record);

    List<ApiScenario> selectByExampleWithBLOBs(ApiScenarioExample example);

    List<ApiScenario> selectByExample(ApiScenarioExample example);

    ApiScenario selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenario record, @Param("example") ApiScenarioExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenario record, @Param("example") ApiScenarioExample example);

    int updateByExample(@Param("record") ApiScenario record, @Param("example") ApiScenarioExample example);

    int updateByPrimaryKeySelective(ApiScenario record);

    int updateByPrimaryKeyWithBLOBs(ApiScenario record);

    int updateByPrimaryKey(ApiScenario record);

    @Select("SELECT COUNT(id) AS countNumber FROM api_scenario WHERE project_id = #{0} ")
    long countByProjectID(String projectId);

    @Select({
            "SELECT count(id) AS countNumber FROM api_scenario ",
            "WHERE project_id = #{projectId} ",
            "AND create_time BETWEEN #{firstDayTimestamp} AND #{lastDayTimestamp} "
    })
    long countByProjectIDAndCreatInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);
}