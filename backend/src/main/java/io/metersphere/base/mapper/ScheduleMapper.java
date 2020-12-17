package io.metersphere.base.mapper;

import io.metersphere.api.dto.dataCount.response.TaskInfoResult;
import io.metersphere.base.domain.Schedule;
import io.metersphere.base.domain.ScheduleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.python.antlr.ast.Str;

public interface ScheduleMapper {
    long countByExample(ScheduleExample example);

    int deleteByExample(ScheduleExample example);

    int deleteByPrimaryKey(String id);

    int insert(Schedule record);

    int insertSelective(Schedule record);

    List<Schedule> selectByExampleWithBLOBs(ScheduleExample example);

    List<Schedule> selectByExample(ScheduleExample example);

    Schedule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Schedule record, @Param("example") ScheduleExample example);

    int updateByExampleWithBLOBs(@Param("record") Schedule record, @Param("example") ScheduleExample example);

    int updateByExample(@Param("record") Schedule record, @Param("example") ScheduleExample example);

    int updateByPrimaryKeySelective(Schedule record);

    int updateByPrimaryKeyWithBLOBs(Schedule record);

    int updateByPrimaryKey(Schedule record);

    @Select("SELECT COUNT(id) AS countNumber FROM `schedule` WHERE `workspace_id` = #{workspaceId} AND `group` = #{group} ")
    long countTaskByWorkspaceIdAndGroup(@Param("workspaceId") String workspaceId,@Param("group") String group);

    @Select({
            "SELECT COUNT(id) AS countNumber FROM `schedule` ",
            "WHERE workspace_id = #{workspaceId} ",
            "AND `group` = #{group} ",
            "AND create_time BETWEEN #{startTime} and #{endTime}; "
    })
    long countTaskByWorkspaceIdAndGroupAndCreateTimeRange(@Param("workspaceId")String workspaceId,@Param("group") String group, @Param("startTime") long startTime, @Param("endTime") long endTime);

    @Select({
            "SELECT apiTest.`name` AS scenario,sch.id AS taskID,sch.`value` AS rule,sch.`enable` AS `taskStatus`,u.`name` AS creator,sch.update_time AS updateTime ",
            "FROM api_test apiTest ",
            "INNER JOIN `schedule` sch ON apiTest.id = sch.resource_id ",
            "INNER JOIN `user` u ON u.id = sch.user_id ",
            "WHERE sch.`enable` = true AND sch.workspace_id = #{0,jdbcType=VARCHAR}"
    })
    List<TaskInfoResult> findRunningTaskInfoByWorkspaceID(String workspaceID);

}