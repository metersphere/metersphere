package io.metersphere.system.mapper;

import io.metersphere.system.domain.PluginBlob;
import io.metersphere.system.domain.PluginBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PluginBlobMapper {
    long countByExample(PluginBlobExample example);

    int deleteByExample(PluginBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(PluginBlob record);

    int insertSelective(PluginBlob record);

    List<PluginBlob> selectByExampleWithBLOBs(PluginBlobExample example);

    List<PluginBlob> selectByExample(PluginBlobExample example);

    PluginBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PluginBlob record, @Param("example") PluginBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") PluginBlob record, @Param("example") PluginBlobExample example);

    int updateByExample(@Param("record") PluginBlob record, @Param("example") PluginBlobExample example);

    int updateByPrimaryKeySelective(PluginBlob record);

    int updateByPrimaryKeyWithBLOBs(PluginBlob record);
}