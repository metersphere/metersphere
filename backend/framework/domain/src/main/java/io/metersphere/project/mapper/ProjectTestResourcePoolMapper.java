package io.metersphere.project.mapper;

import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProjectTestResourcePoolMapper {
    long countByExample(ProjectTestResourcePoolExample example);

    int deleteByExample(ProjectTestResourcePoolExample example);

    int deleteByPrimaryKey(@Param("projectId") String projectId, @Param("testResourcePoolId") String testResourcePoolId);

    int insert(ProjectTestResourcePool record);

    int insertSelective(ProjectTestResourcePool record);

    List<ProjectTestResourcePool> selectByExample(ProjectTestResourcePoolExample example);

    int updateByExampleSelective(@Param("record") ProjectTestResourcePool record, @Param("example") ProjectTestResourcePoolExample example);

    int updateByExample(@Param("record") ProjectTestResourcePool record, @Param("example") ProjectTestResourcePoolExample example);

    int batchInsert(@Param("list") List<ProjectTestResourcePool> list);

    int batchInsertSelective(@Param("list") List<ProjectTestResourcePool> list, @Param("selective") ProjectTestResourcePool.Column ... selective);
}