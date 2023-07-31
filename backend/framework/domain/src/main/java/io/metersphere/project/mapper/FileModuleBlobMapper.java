package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileModuleBlob;
import io.metersphere.project.domain.FileModuleBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileModuleBlobMapper {
    long countByExample(FileModuleBlobExample example);

    int deleteByExample(FileModuleBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(FileModuleBlob record);

    int insertSelective(FileModuleBlob record);

    List<FileModuleBlob> selectByExampleWithBLOBs(FileModuleBlobExample example);

    List<FileModuleBlob> selectByExample(FileModuleBlobExample example);

    FileModuleBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FileModuleBlob record, @Param("example") FileModuleBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") FileModuleBlob record, @Param("example") FileModuleBlobExample example);

    int updateByExample(@Param("record") FileModuleBlob record, @Param("example") FileModuleBlobExample example);

    int updateByPrimaryKeySelective(FileModuleBlob record);

    int updateByPrimaryKeyWithBLOBs(FileModuleBlob record);

    int updateByPrimaryKey(FileModuleBlob record);

    int batchInsert(@Param("list") List<FileModuleBlob> list);

    int batchInsertSelective(@Param("list") List<FileModuleBlob> list, @Param("selective") FileModuleBlob.Column ... selective);
}