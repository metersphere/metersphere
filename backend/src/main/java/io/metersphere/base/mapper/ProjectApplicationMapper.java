package io.metersphere.base.mapper;

import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.base.domain.ProjectApplicationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProjectApplicationMapper {
    long countByExample(ProjectApplicationExample example);

    int deleteByExample(ProjectApplicationExample example);

    int insert(ProjectApplication record);

    int insertSelective(ProjectApplication record);

    List<ProjectApplication> selectByExample(ProjectApplicationExample example);

    int updateByExampleSelective(@Param("record") ProjectApplication record, @Param("example") ProjectApplicationExample example);

    int updateByExample(@Param("record") ProjectApplication record, @Param("example") ProjectApplicationExample example);
}