package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionMockConfig;
import io.metersphere.api.domain.ApiDefinitionMockConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionMockConfigMapper {
    long countByExample(ApiDefinitionMockConfigExample example);

    int deleteByExample(ApiDefinitionMockConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionMockConfig record);

    int insertSelective(ApiDefinitionMockConfig record);

    List<ApiDefinitionMockConfig> selectByExampleWithBLOBs(ApiDefinitionMockConfigExample example);

    List<ApiDefinitionMockConfig> selectByExample(ApiDefinitionMockConfigExample example);

    ApiDefinitionMockConfig selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionMockConfig record, @Param("example") ApiDefinitionMockConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDefinitionMockConfig record, @Param("example") ApiDefinitionMockConfigExample example);

    int updateByExample(@Param("record") ApiDefinitionMockConfig record, @Param("example") ApiDefinitionMockConfigExample example);

    int updateByPrimaryKeySelective(ApiDefinitionMockConfig record);

    int updateByPrimaryKeyWithBLOBs(ApiDefinitionMockConfig record);

    int batchInsert(@Param("list") List<ApiDefinitionMockConfig> list);

    int batchInsertSelective(@Param("list") List<ApiDefinitionMockConfig> list, @Param("selective") ApiDefinitionMockConfig.Column ... selective);
}