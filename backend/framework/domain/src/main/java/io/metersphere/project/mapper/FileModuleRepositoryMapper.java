package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileModuleRepository;
import io.metersphere.project.domain.FileModuleRepositoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileModuleRepositoryMapper {
    long countByExample(FileModuleRepositoryExample example);

    int deleteByExample(FileModuleRepositoryExample example);

    int deleteByPrimaryKey(String fileModuleId);

    int insert(FileModuleRepository record);

    int insertSelective(FileModuleRepository record);

    List<FileModuleRepository> selectByExample(FileModuleRepositoryExample example);

    FileModuleRepository selectByPrimaryKey(String fileModuleId);

    int updateByExampleSelective(@Param("record") FileModuleRepository record, @Param("example") FileModuleRepositoryExample example);

    int updateByExample(@Param("record") FileModuleRepository record, @Param("example") FileModuleRepositoryExample example);

    int updateByPrimaryKeySelective(FileModuleRepository record);

    int updateByPrimaryKey(FileModuleRepository record);

    int batchInsert(@Param("list") List<FileModuleRepository> list);

    int batchInsertSelective(@Param("list") List<FileModuleRepository> list, @Param("selective") FileModuleRepository.Column ... selective);
}