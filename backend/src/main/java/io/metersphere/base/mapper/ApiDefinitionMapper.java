package io.metersphere.base.mapper;

import io.metersphere.api.dto.definition.ApiComputeResult;
import io.metersphere.api.dto.definition.ApiDefinitionRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiDefinitionMapper {

    List<ApiDefinitionResult> list(@Param("request") ApiDefinitionRequest request);

    List<ApiComputeResult> selectByIds(@Param("ids") List<String> ids);

    long countByExample(ApiDefinitionExample example);

    int deleteByExample(ApiDefinitionExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinition record);


    List<ApiDefinition> selectByExampleWithBLOBs(ApiDefinitionExample example);

    List<ApiDefinition> selectByExample(ApiDefinitionExample example);

    ApiDefinition selectByPrimaryKey(String id);


    int updateByPrimaryKeySelective(ApiDefinition record);

    int updateByPrimaryKeyWithBLOBs(ApiDefinition record);

    int updateByPrimaryKey(ApiDefinition record);

    int removeToGc(@Param("ids") List<String> ids);

}