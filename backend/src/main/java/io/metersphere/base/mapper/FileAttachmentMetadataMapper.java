package io.metersphere.base.mapper;

import io.metersphere.base.domain.FileAttachmentMetadata;
import io.metersphere.base.domain.FileAttachmentMetadataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileAttachmentMetadataMapper {
    long countByExample(FileAttachmentMetadataExample example);

    int deleteByExample(FileAttachmentMetadataExample example);

    int deleteByPrimaryKey(String id);

    int insert(FileAttachmentMetadata record);

    int insertSelective(FileAttachmentMetadata record);

    List<FileAttachmentMetadata> selectByExample(FileAttachmentMetadataExample example);

    FileAttachmentMetadata selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FileAttachmentMetadata record, @Param("example") FileAttachmentMetadataExample example);

    int updateByExample(@Param("record") FileAttachmentMetadata record, @Param("example") FileAttachmentMetadataExample example);

    int updateByPrimaryKeySelective(FileAttachmentMetadata record);

    int updateByPrimaryKey(FileAttachmentMetadata record);
}