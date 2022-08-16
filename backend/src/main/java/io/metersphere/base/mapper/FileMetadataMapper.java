package io.metersphere.base.mapper;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileMetadataMapper {
    long countByExample(FileMetadataExample example);

    int deleteByExample(FileMetadataExample example);

    int deleteByPrimaryKey(String id);

    int insert(FileMetadata record);

    int insertSelective(FileMetadata record);

    List<FileMetadata> selectByExampleWithBLOBs(FileMetadataExample example);

    List<FileMetadata> selectByExample(FileMetadataExample example);

    FileMetadata selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FileMetadata record, @Param("example") FileMetadataExample example);

    int updateByExampleWithBLOBs(@Param("record") FileMetadata record, @Param("example") FileMetadataExample example);

    int updateByExample(@Param("record") FileMetadata record, @Param("example") FileMetadataExample example);

    int updateByPrimaryKeySelective(FileMetadata record);

    int updateByPrimaryKeyWithBLOBs(FileMetadata record);

    int updateByPrimaryKey(FileMetadata record);
}