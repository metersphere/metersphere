package io.metersphere.base.mapper;

import io.metersphere.api.dto.dataCount.ApiDataCountResult;
import io.metersphere.api.dto.dataCount.response.ApiDataCountDTO;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionExample;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ApiDefinitionMapper {
    long countByExample(ApiDefinitionExample example);

    int deleteByExample(ApiDefinitionExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionWithBLOBs record);

    int insertSelective(ApiDefinitionWithBLOBs record);

    List<ApiDefinitionWithBLOBs> selectByExampleWithBLOBs(ApiDefinitionExample example);

    List<ApiDefinition> selectByExample(ApiDefinitionExample example);

    ApiDefinitionWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionWithBLOBs record, @Param("example") ApiDefinitionExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDefinitionWithBLOBs record, @Param("example") ApiDefinitionExample example);

    int updateByExample(@Param("record") ApiDefinition record, @Param("example") ApiDefinitionExample example);

    int updateByPrimaryKeySelective(ApiDefinitionWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiDefinitionWithBLOBs record);

    int updateByPrimaryKey(ApiDefinition record);

    @Select("SELECT protocol AS groupField,count(id) AS countNumber FROM api_definition  WHERE project_id = #{0} GROUP BY protocol;")
    List<ApiDataCountResult> countProtocolByProjectID(String projectId);

    @Select({
            "SELECT count(id) AS countNumber FROM api_definition ",
            "WHERE project_id = #{projectId} ",
            "AND create_time BETWEEN #{firstDayTimestamp} AND #{lastDayTimestamp} "
    })
    long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);
}