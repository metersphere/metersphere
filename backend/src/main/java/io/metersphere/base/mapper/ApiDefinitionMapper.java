package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionExample;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionMapper {
    long countByExample(ApiDefinitionExample example);

    int deleteByExample(ApiDefinitionExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionWithBLOBs record);

    int insertSelective(ApiDefinitionWithBLOBs record);

    List<ApiDefinitionWithBLOBs> selectByExampleWithBLOBs(ApiDefinitionExample example);

    List<ApiDefinition> selectByExample(ApiDefinitionExample example);

    ApiDefinitionWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionWithBLOBs record, @Param("example") ApiDefinitionExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDefinitionWithBLOBs record, @Param("example") ApiDefinitionExample example);

    int updateByExample(@Param("record") ApiDefinition record, @Param("example") ApiDefinitionExample example);

    int updateByPrimaryKeySelective(ApiDefinitionWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiDefinitionWithBLOBs record);

    int updateByPrimaryKey(ApiDefinition record);
}