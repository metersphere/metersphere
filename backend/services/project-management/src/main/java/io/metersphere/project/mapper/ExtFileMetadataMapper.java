package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.filemanagement.FileManagementPageDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFileMetadataMapper {
    List<FileMetadata> selectByKeywordAndFileType(FileManagementPageDTO fileManagementPageDTO);

    List<FileMetadata> selectRefIdByKeywordAndFileType(FileManagementPageDTO fileManagementPageDTO);

    List<ModuleCountDTO> countModuleIdByKeywordAndFileType(FileManagementPageDTO fileManagementPageDTO);

    long countMyFile(FileManagementPageDTO fileManagementPageDTO);

    FileMetadata getById(String id);

    List<String> selectIdByRefIdList(@Param("refIdList") List<String> refIdList);

    List<FileMetadata> selectDeleteFileInfoByIds(@Param("ids") List<String> ids);

    List<FileMetadata> selectDeleteFileInfoByRefIdList(@Param("refIdList") List<String> refIdList);

    List<FileMetadata> selectRefIdByIds(@Param("fileIds") List<String> processIds);

    List<FileMetadata> selectRefIdByModuleIds(@Param("moduleIds") List<String> moduleIds);

    List<String> selectFileTypeByProjectId(String projectId);
}
