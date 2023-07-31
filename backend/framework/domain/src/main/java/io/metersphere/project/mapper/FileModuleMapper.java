package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileModule;
import io.metersphere.project.domain.FileModuleExample;
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

    int batchInsert(@Param("list") List<FileModule> list);

    int batchInsertSelective(@Param("list") List<FileModule> list, @Param("selective") FileModule.Column ... selective);
}