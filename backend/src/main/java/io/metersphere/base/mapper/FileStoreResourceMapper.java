package io.metersphere.base.mapper;

import io.metersphere.base.domain.FileStoreResource;
import io.metersphere.base.domain.FileStoreResourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileStoreResourceMapper {
    long countByExample(FileStoreResourceExample example);

    int deleteByExample(FileStoreResourceExample example);

    int insert(FileStoreResource record);

    int insertSelective(FileStoreResource record);

    List<FileStoreResource> selectByExample(FileStoreResourceExample example);

    int updateByExampleSelective(@Param("record") FileStoreResource record, @Param("example") FileStoreResourceExample example);

    int updateByExample(@Param("record") FileStoreResource record, @Param("example") FileStoreResourceExample example);
}