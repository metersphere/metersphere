package io.metersphere.base.mapper;

import io.metersphere.base.domain.FileModule;
import io.metersphere.base.domain.FileModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileModuleMapper {
    long countByExample(FileModuleExample example);

    int deleteByExample(FileModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(FileModule record);

    int insertSelective(FileModule record);

    List<FileModule> selectByExample(FileModuleExample example);

    FileModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FileModule record, @Param("example") FileModuleExample example);

    int updateByExample(@Param("record") FileModule record, @Param("example") FileModuleExample example);

    int updateByPrimaryKeySelective(FileModule record);

    int updateByPrimaryKey(FileModule record);
}