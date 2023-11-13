package io.metersphere.project.mapper;

import org.apache.ibatis.annotations.Param;

public interface ExtBaseProjectVersionMapper {

    String getDefaultVersion(@Param("projectId") String projectId);
}
