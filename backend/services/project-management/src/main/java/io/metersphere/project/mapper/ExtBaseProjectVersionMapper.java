package io.metersphere.project.mapper;

import io.metersphere.project.domain.ProjectVersion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtBaseProjectVersionMapper {

    String getDefaultVersion(@Param("projectId") String projectId);


    List<ProjectVersion> getVersionByIds(@Param("ids") List<String> ids);
}
