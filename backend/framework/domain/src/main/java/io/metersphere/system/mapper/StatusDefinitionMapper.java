package io.metersphere.system.mapper;

import io.metersphere.system.domain.StatusDefinition;
import io.metersphere.system.domain.StatusDefinitionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StatusDefinitionMapper {
    long countByExample(StatusDefinitionExample example);

    int deleteByExample(StatusDefinitionExample example);

    int deleteByPrimaryKey(String id);

    int insert(StatusDefinition record);

    int insertSelective(StatusDefinition record);

    List<StatusDefinition> selectByExample(StatusDefinitionExample example);

    StatusDefinition selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") StatusDefinition record, @Param("example") StatusDefinitionExample example);

    int updateByExample(@Param("record") StatusDefinition record, @Param("example") StatusDefinitionExample example);

    int updateByPrimaryKeySelective(StatusDefinition record);

    int updateByPrimaryKey(StatusDefinition record);

    int batchInsert(@Param("list") List<StatusDefinition> list);

    int batchInsertSelective(@Param("list") List<StatusDefinition> list, @Param("selective") StatusDefinition.Column ... selective);
}