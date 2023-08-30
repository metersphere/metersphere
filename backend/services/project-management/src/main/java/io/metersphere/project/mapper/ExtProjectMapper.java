package io.metersphere.project.mapper;

import io.metersphere.project.domain.Project;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtProjectMapper {

    List<Project> getUserProject(@Param("organizationId") String organizationId, @Param("userId") String userId);
}
