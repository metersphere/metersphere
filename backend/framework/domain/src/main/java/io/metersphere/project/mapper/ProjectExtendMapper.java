package io.metersphere.project.mapper;

import io.metersphere.project.domain.ProjectExtend;
import io.metersphere.project.domain.ProjectExtendExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProjectExtendMapper {
    long countByExample(ProjectExtendExample example);

    int deleteByExample(ProjectExtendExample example);

    int deleteByPrimaryKey(String id);

    int insert(ProjectExtend record);

    int insertSelective(ProjectExtend record);

    List<ProjectExtend> selectByExample(ProjectExtendExample example);

    ProjectExtend selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ProjectExtend record, @Param("example") ProjectExtendExample example);

    int updateByExample(@Param("record") ProjectExtend record, @Param("example") ProjectExtendExample example);

    int updateByPrimaryKeySelective(ProjectExtend record);

    int updateByPrimaryKey(ProjectExtend record);

    int batchInsert(@Param("list") List<ProjectExtend> list);

    int batchInsertSelective(@Param("list") List<ProjectExtend> list, @Param("selective") ProjectExtend.Column ... selective);
}