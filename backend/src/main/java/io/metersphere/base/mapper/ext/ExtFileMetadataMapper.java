package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.metadata.vo.MoveFIleMetadataRequest;
import io.metersphere.performance.request.QueryProjectFileRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtFileMetadataMapper {
    List<FileMetadata> getProjectFiles(@Param("projectId") String projectId, @Param("request") QueryProjectFileRequest request);

    List<String> getTypes();

    void move(@Param("request") MoveFIleMetadataRequest request);

    List<Map<String, Object>> moduleCountByMetadataIds(@Param("ids") List<String> ids);

    void updateModuleIdByProjectId(@Param("moduleId")String moduleId, @Param("projectId")String projectId);
}