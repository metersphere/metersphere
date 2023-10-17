package io.metersphere.system.mapper;

import io.metersphere.system.domain.StatusDefinition;
import io.metersphere.system.domain.StatusDefinitionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StatusDefinitionMapper {
    long countByExample(StatusDefinitionExample example);

    int deleteByExample(StatusDefinitionExample example);

    int deleteByPrimaryKey(@Param("statusId") String statusId, @Param("definitionId") String definitionId);

    int insert(StatusDefinition record);

    int insertSelective(StatusDefinition record);

    List<StatusDefinition> selectByExample(StatusDefinitionExample example);

    int updateByExampleSelective(@Param("record") StatusDefinition record, @Param("example") StatusDefinitionExample example);

    int updateByExample(@Param("record") StatusDefinition record, @Param("example") StatusDefinitionExample example);

    int batchInsert(@Param("list") List<StatusDefinition> list);

    int batchInsertSelective(@Param("list") List<StatusDefinition> list, @Param("selective") StatusDefinition.Column ... selective);
}