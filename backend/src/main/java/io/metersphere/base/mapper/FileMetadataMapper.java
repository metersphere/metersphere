package io.metersphere.base.mapper;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataExample;
import io.metersphere.base.domain.FileMetadataWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileMetadataMapper {
    long countByExample(FileMetadataExample example);

    int deleteByExample(FileMetadataExample example);

    int deleteByPrimaryKey(String id);

    int insert(FileMetadataWithBLOBs record);

    int insertSelective(FileMetadataWithBLOBs record);

    List<FileMetadataWithBLOBs> selectByExampleWithBLOBs(FileMetadataExample example);

    List<FileMetadata> selectByExample(FileMetadataExample example);

    FileMetadataWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FileMetadataWithBLOBs record, @Param("example") FileMetadataExample example);

    int updateByExampleWithBLOBs(@Param("record") FileMetadataWithBLOBs record, @Param("example") FileMetadataExample example);

    int updateByExample(@Param("record") FileMetadata record, @Param("example") FileMetadataExample example);

    int updateByPrimaryKeySelective(FileMetadataWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(FileMetadataWithBLOBs record);

    int updateByPrimaryKey(FileMetadata record);
}