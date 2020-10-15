package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiTestJarConfig;
import io.metersphere.base.domain.ApiTestJarConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestJarConfigMapper {
    long countByExample(ApiTestJarConfigExample example);

    int deleteByExample(ApiTestJarConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTestJarConfig record);

    int insertSelective(ApiTestJarConfig record);

    List<ApiTestJarConfig> selectByExample(ApiTestJarConfigExample example);

    ApiTestJarConfig selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTestJarConfig record, @Param("example") ApiTestJarConfigExample example);

    int updateByExample(@Param("record") ApiTestJarConfig record, @Param("example") ApiTestJarConfigExample example);

    int updateByPrimaryKeySelective(ApiTestJarConfig record);

    int updateByPrimaryKey(ApiTestJarConfig record);
}