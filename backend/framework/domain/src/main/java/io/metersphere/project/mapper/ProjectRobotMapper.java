package io.metersphere.project.mapper;

import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.domain.ProjectRobotExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProjectRobotMapper {
    long countByExample(ProjectRobotExample example);

    int deleteByExample(ProjectRobotExample example);

    int deleteByPrimaryKey(String id);

    int insert(ProjectRobot record);

    int insertSelective(ProjectRobot record);

    List<ProjectRobot> selectByExample(ProjectRobotExample example);

    ProjectRobot selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ProjectRobot record, @Param("example") ProjectRobotExample example);

    int updateByExample(@Param("record") ProjectRobot record, @Param("example") ProjectRobotExample example);

    int updateByPrimaryKeySelective(ProjectRobot record);

    int updateByPrimaryKey(ProjectRobot record);

    int batchInsert(@Param("list") List<ProjectRobot> list);

    int batchInsertSelective(@Param("list") List<ProjectRobot> list, @Param("selective") ProjectRobot.Column ... selective);
}