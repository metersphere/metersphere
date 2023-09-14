package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionModule;
import io.metersphere.api.domain.ApiDefinitionModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionModuleMapper {
    long countByExample(ApiDefinitionModuleExample example);

    int deleteByExample(ApiDefinitionModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionModule record);

    int insertSelective(ApiDefinitionModule record);

    List<ApiDefinitionModule> selectByExample(ApiDefinitionModuleExample example);

    ApiDefinitionModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionModule record, @Param("example") ApiDefinitionModuleExample example);

    int updateByExample(@Param("record") ApiDefinitionModule record, @Param("example") ApiDefinitionModuleExample example);

    int updateByPrimaryKeySelective(ApiDefinitionModule record);

    int updateByPrimaryKey(ApiDefinitionModule record);

    int batchInsert(@Param("list") List<ApiDefinitionModule> list);

    int batchInsertSelective(@Param("list") List<ApiDefinitionModule> list, @Param("selective") ApiDefinitionModule.Column ... selective);
}