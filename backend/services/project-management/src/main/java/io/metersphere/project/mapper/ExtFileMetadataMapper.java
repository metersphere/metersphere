package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.filemanagement.FileManagementQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFileMetadataMapper {
    List<FileMetadata> selectByKeywordAndFileType(FileManagementQuery fileManagementQuery);

    List<FileMetadata> selectRefIdByKeywordAndFileType(FileManagementQuery fileManagementQuery);

    List<ModuleCountDTO> countModuleIdByKeywordAndFileType(FileManagementQuery fileManagementQuery);

    long fileCount(FileManagementQuery fileManagementQuery);

    FileMetadata getById(String id);

    List<String> selectIdByRefIdList(@Param("refIdList") List<String> refIdList);

    List<FileMetadata> selectDeleteFileInfoByIds(@Param("ids") List<String> ids);

    List<FileMetadata> selectDeleteFileInfoByRefIdList(@Param("refIdList") List<String> refIdList);

    List<FileMetadata> selectRefIdByIds(@Param("fileIds") List<String> processIds);

    List<FileMetadata> selectRefIdByModuleIds(@Param("moduleIds") List<String> moduleIds);

    List<String> selectFileTypeByProjectId(@Param("projectId") String projectId, @Param("storage") String storage);

    long countRepositoryFileByFileNameAndBranch(@Param("projectId") String projectId, @Param("moduleId") String moduleId, @Param("filePath") String filePath, @Param("branch") String branch);

    FileMetadata selectLatestById(String fileId);
}
