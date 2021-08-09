package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiDefinitionEnv;
import io.metersphere.base.domain.ApiDefinitionEnvExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionEnvMapper {
    long countByExample(ApiDefinitionEnvExample example);

    int deleteByExample(ApiDefinitionEnvExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionEnv record);

    int insertSelective(ApiDefinitionEnv record);

    List<ApiDefinitionEnv> selectByExample(ApiDefinitionEnvExample example);

    ApiDefinitionEnv selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionEnv record, @Param("example") ApiDefinitionEnvExample example);

    int updateByExample(@Param("record") ApiDefinitionEnv record, @Param("example") ApiDefinitionEnvExample example);

    int updateByPrimaryKeySelective(ApiDefinitionEnv record);

    int updateByPrimaryKey(ApiDefinitionEnv record);
}