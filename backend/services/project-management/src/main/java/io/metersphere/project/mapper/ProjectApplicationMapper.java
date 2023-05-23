package io.metersphere.project.mapper;

import io.metersphere.project.domain.ProjectApplication;
import org.apache.ibatis.annotations.Param;

public interface ProjectApplicationMapper {

    void update(@Param("application") ProjectApplication application);

    void insert(@Param("application") ProjectApplication application);
}
