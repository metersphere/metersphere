package io.metersphere.base.mapper;

import io.metersphere.base.domain.FileStore;
import io.metersphere.base.domain.FileStoreExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileStoreMapper {
    long countByExample(FileStoreExample example);

    int deleteByExample(FileStoreExample example);

    int deleteByPrimaryKey(String id);

    int insert(FileStore record);

    int insertSelective(FileStore record);

    List<FileStore> selectByExampleWithBLOBs(FileStoreExample example);

    List<FileStore> selectByExample(FileStoreExample example);

    FileStore selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FileStore record, @Param("example") FileStoreExample example);

    int updateByExampleWithBLOBs(@Param("record") FileStore record, @Param("example") FileStoreExample example);

    int updateByExample(@Param("record") FileStore record, @Param("example") FileStoreExample example);

    int updateByPrimaryKeySelective(FileStore record);

    int updateByPrimaryKeyWithBLOBs(FileStore record);

    int updateByPrimaryKey(FileStore record);
}