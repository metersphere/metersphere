package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.dto.ModuleCountDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFileMetadataMapper {
    List<FileMetadata> selectByKeywordAndFileType(@Param("projectId") String projectId, @Param("keyword") String keyword, @Param("moduleIds") List<String> moduleIds, @Param("fileTypes") List<String> fileTypes, @Param("isRefId") boolean isRefId);

    List<ModuleCountDTO> countModuleIdByKeywordAndFileType(@Param("projectId") String projectId, @Param("keyword") String keyword, @Param("moduleIds") List<String> moduleIds, @Param("fileTypes") List<String> fileTypes);

    List<String> selectIdByRefIdList(@Param("refIdList") List<String> refIdList);

    List<FileMetadata> selectDeleteFileInfoByIds(@Param("ids") List<String> ids);

    List<FileMetadata> selectDeleteFileInfoByRefIdList(@Param("refIdList") List<String> refIdList);

    List<FileMetadata> selectRefIdByIds(@Param("fileIds") List<String> processIds);

    List<FileMetadata> selectRefIdByModuleIds(@Param("moduleIds") List<String> moduleIds);
}
