package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionMapper {
    long countByExample(ApiDefinitionExample example);

    int deleteByExample(ApiDefinitionExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinition record);

    int insertSelective(ApiDefinition record);

    List<ApiDefinition> selectByExample(ApiDefinitionExample example);

    ApiDefinition selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinition record, @Param("example") ApiDefinitionExample example);

    int updateByExample(@Param("record") ApiDefinition record, @Param("example") ApiDefinitionExample example);

    int updateByPrimaryKeySelective(ApiDefinition record);

    int updateByPrimaryKey(ApiDefinition record);

    int batchInsert(@Param("list") List<ApiDefinition> list);

    int batchInsertSelective(@Param("list") List<ApiDefinition> list, @Param("selective") ApiDefinition.Column ... selective);
}