package io.metersphere.base.mapper.ext;

import io.metersphere.request.ProjectVersionRequest;
import io.metersphere.dto.ProjectVersionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseProjectVersionMapper {
    List<ProjectVersionDTO> selectProjectVersionList(@Param("request") ProjectVersionRequest request);

    String getDefaultVersion(@Param("projectId") String projectId);

    void updateLatestToFalse(@Param("projectId") String projectId);

    boolean isVersionEnable(@Param("projectId") String projectId);

    void changeVersionEnable(@Param("projectId") String projectId, @Param("status") boolean status);

    boolean checkForDelete(String id);
}
