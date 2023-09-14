package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiSyncConfig;
import io.metersphere.api.domain.ApiSyncConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiSyncConfigMapper {
    long countByExample(ApiSyncConfigExample example);

    int deleteByExample(ApiSyncConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiSyncConfig record);

    int insertSelective(ApiSyncConfig record);

    List<ApiSyncConfig> selectByExampleWithBLOBs(ApiSyncConfigExample example);

    List<ApiSyncConfig> selectByExample(ApiSyncConfigExample example);

    ApiSyncConfig selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiSyncConfig record, @Param("example") ApiSyncConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiSyncConfig record, @Param("example") ApiSyncConfigExample example);

    int updateByExample(@Param("record") ApiSyncConfig record, @Param("example") ApiSyncConfigExample example);

    int updateByPrimaryKeySelective(ApiSyncConfig record);

    int updateByPrimaryKeyWithBLOBs(ApiSyncConfig record);

    int updateByPrimaryKey(ApiSyncConfig record);

    int batchInsert(@Param("list") List<ApiSyncConfig> list);

    int batchInsertSelective(@Param("list") List<ApiSyncConfig> list, @Param("selective") ApiSyncConfig.Column ... selective);
}