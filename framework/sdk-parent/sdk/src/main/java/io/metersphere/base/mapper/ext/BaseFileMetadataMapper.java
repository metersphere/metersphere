package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.FileMetadataWithBLOBs;
import io.metersphere.dto.FileMetadataDTO;
import io.metersphere.metadata.vo.MoveFIleMetadataRequest;
import io.metersphere.request.QueryProjectFileRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BaseFileMetadataMapper {
    List<FileMetadataWithBLOBs> getProjectFiles(@Param("projectId") String projectId, @Param("request") QueryProjectFileRequest request);

    List<FileMetadataDTO> getFileMetadataByProject(@Param("projectId") String projectId, @Param("request") QueryProjectFileRequest request);

    List<String> getTypes();

    void move(@Param("request") MoveFIleMetadataRequest request);

    List<Map<String, Object>> moduleCountByMetadataIds(@Param("ids") List<String> ids);

    void updateModuleIdByProjectId(@Param("moduleId") String moduleId, @Param("projectId") String projectId);

    List<String> selectRefIdsByIds(@Param("ids") List<String> nodeIds);

    List<String> selectIllegalModuleIdListByProjectId(String projectId);
}