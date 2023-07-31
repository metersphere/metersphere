package io.metersphere.project.mapper;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProjectApplicationMapper {
    long countByExample(ProjectApplicationExample example);

    int deleteByExample(ProjectApplicationExample example);

    int deleteByPrimaryKey(@Param("projectId") String projectId, @Param("type") String type);

    int insert(ProjectApplication record);

    int insertSelective(ProjectApplication record);

    List<ProjectApplication> selectByExample(ProjectApplicationExample example);

    ProjectApplication selectByPrimaryKey(@Param("projectId") String projectId, @Param("type") String type);

    int updateByExampleSelective(@Param("record") ProjectApplication record, @Param("example") ProjectApplicationExample example);

    int updateByExample(@Param("record") ProjectApplication record, @Param("example") ProjectApplicationExample example);

    int updateByPrimaryKeySelective(ProjectApplication record);

    int updateByPrimaryKey(ProjectApplication record);

    int batchInsert(@Param("list") List<ProjectApplication> list);

    int batchInsertSelective(@Param("list") List<ProjectApplication> list, @Param("selective") ProjectApplication.Column ... selective);
}