package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileMetadataBlob;
import io.metersphere.project.domain.FileMetadataBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileMetadataBlobMapper {
    long countByExample(FileMetadataBlobExample example);

    int deleteByExample(FileMetadataBlobExample example);

    int deleteByPrimaryKey(String fileId);

    int insert(FileMetadataBlob record);

    int insertSelective(FileMetadataBlob record);

    List<FileMetadataBlob> selectByExampleWithBLOBs(FileMetadataBlobExample example);

    List<FileMetadataBlob> selectByExample(FileMetadataBlobExample example);

    FileMetadataBlob selectByPrimaryKey(String fileId);

    int updateByExampleSelective(@Param("record") FileMetadataBlob record, @Param("example") FileMetadataBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") FileMetadataBlob record, @Param("example") FileMetadataBlobExample example);

    int updateByExample(@Param("record") FileMetadataBlob record, @Param("example") FileMetadataBlobExample example);

    int updateByPrimaryKeySelective(FileMetadataBlob record);

    int updateByPrimaryKeyWithBLOBs(FileMetadataBlob record);
}