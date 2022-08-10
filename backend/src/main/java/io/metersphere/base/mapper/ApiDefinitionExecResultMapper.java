package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExample;
import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionExecResultMapper {
    long countByExample(ApiDefinitionExecResultExample example);

    int deleteByExample(ApiDefinitionExecResultExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionExecResultWithBLOBs record);

    int insertSelective(ApiDefinitionExecResultWithBLOBs record);

    List<ApiDefinitionExecResultWithBLOBs> selectByExampleWithBLOBs(ApiDefinitionExecResultExample example);

    List<ApiDefinitionExecResult> selectByExample(ApiDefinitionExecResultExample example);

    ApiDefinitionExecResultWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionExecResultWithBLOBs record, @Param("example") ApiDefinitionExecResultExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDefinitionExecResultWithBLOBs record, @Param("example") ApiDefinitionExecResultExample example);

    int updateByExample(@Param("record") ApiDefinitionExecResult record, @Param("example") ApiDefinitionExecResultExample example);

    int updateByPrimaryKeySelective(ApiDefinitionExecResultWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiDefinitionExecResultWithBLOBs record);

    int updateByPrimaryKey(ApiDefinitionExecResult record);
}