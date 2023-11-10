package io.metersphere.project.mapper;

import org.apache.ibatis.annotations.Param;

public interface ExtProjectVersionMapper {

    String getDefaultVersion(@Param("projectId") String projectId);
}
