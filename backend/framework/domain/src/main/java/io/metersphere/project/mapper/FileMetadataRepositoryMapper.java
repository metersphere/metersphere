package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileMetadataRepository;
import io.metersphere.project.domain.FileMetadataRepositoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileMetadataRepositoryMapper {
    long countByExample(FileMetadataRepositoryExample example);

    int deleteByExample(FileMetadataRepositoryExample example);

    int deleteByPrimaryKey(String fileMetadataId);

    int insert(FileMetadataRepository record);

    int insertSelective(FileMetadataRepository record);

    List<FileMetadataRepository> selectByExampleWithBLOBs(FileMetadataRepositoryExample example);

    List<FileMetadataRepository> selectByExample(FileMetadataRepositoryExample example);

    FileMetadataRepository selectByPrimaryKey(String fileMetadataId);

    int updateByExampleSelective(@Param("record") FileMetadataRepository record, @Param("example") FileMetadataRepositoryExample example);

    int updateByExampleWithBLOBs(@Param("record") FileMetadataRepository record, @Param("example") FileMetadataRepositoryExample example);

    int updateByExample(@Param("record") FileMetadataRepository record, @Param("example") FileMetadataRepositoryExample example);

    int updateByPrimaryKeySelective(FileMetadataRepository record);

    int updateByPrimaryKeyWithBLOBs(FileMetadataRepository record);

    int updateByPrimaryKey(FileMetadataRepository record);

    int batchInsert(@Param("list") List<FileMetadataRepository> list);

    int batchInsertSelective(@Param("list") List<FileMetadataRepository> list, @Param("selective") FileMetadataRepository.Column ... selective);
}