package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiDefinitionExecResultMapper {
    long countByExample(ApiDefinitionExecResultExample example);

    int deleteByExample(ApiDefinitionExecResultExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionExecResult record);

    int insertSelective(ApiDefinitionExecResult record);

    List<ApiDefinitionExecResult> selectByExampleWithBLOBs(ApiDefinitionExecResultExample example);

    List<ApiDefinitionExecResult> selectByExample(ApiDefinitionExecResultExample example);

    ApiDefinitionExecResult selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionExecResult record, @Param("example") ApiDefinitionExecResultExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDefinitionExecResult record, @Param("example") ApiDefinitionExecResultExample example);

    int updateByExample(@Param("record") ApiDefinitionExecResult record, @Param("example") ApiDefinitionExecResultExample example);

    int updateByPrimaryKeySelective(ApiDefinitionExecResult record);

    int updateByPrimaryKeyWithBLOBs(ApiDefinitionExecResult record);

    int updateByPrimaryKey(ApiDefinitionExecResult record);
}