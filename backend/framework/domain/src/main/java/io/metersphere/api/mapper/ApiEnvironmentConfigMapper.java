package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiEnvironmentConfig;
import io.metersphere.api.domain.ApiEnvironmentConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiEnvironmentConfigMapper {
    long countByExample(ApiEnvironmentConfigExample example);

    int deleteByExample(ApiEnvironmentConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiEnvironmentConfig record);

    int insertSelective(ApiEnvironmentConfig record);

    List<ApiEnvironmentConfig> selectByExample(ApiEnvironmentConfigExample example);

    ApiEnvironmentConfig selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiEnvironmentConfig record, @Param("example") ApiEnvironmentConfigExample example);

    int updateByExample(@Param("record") ApiEnvironmentConfig record, @Param("example") ApiEnvironmentConfigExample example);

    int updateByPrimaryKeySelective(ApiEnvironmentConfig record);

    int updateByPrimaryKey(ApiEnvironmentConfig record);
}