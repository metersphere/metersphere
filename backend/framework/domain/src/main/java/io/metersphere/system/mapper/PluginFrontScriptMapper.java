package io.metersphere.system.mapper;

import io.metersphere.system.domain.PluginFrontScript;
import io.metersphere.system.domain.PluginFrontScriptExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PluginFrontScriptMapper {
    long countByExample(PluginFrontScriptExample example);

    int deleteByExample(PluginFrontScriptExample example);

    int deleteByPrimaryKey(@Param("pluginId") String pluginId, @Param("scriptId") String scriptId);

    int insert(PluginFrontScript record);

    int insertSelective(PluginFrontScript record);

    List<PluginFrontScript> selectByExampleWithBLOBs(PluginFrontScriptExample example);

    List<PluginFrontScript> selectByExample(PluginFrontScriptExample example);

    PluginFrontScript selectByPrimaryKey(@Param("pluginId") String pluginId, @Param("scriptId") String scriptId);

    int updateByExampleSelective(@Param("record") PluginFrontScript record, @Param("example") PluginFrontScriptExample example);

    int updateByExampleWithBLOBs(@Param("record") PluginFrontScript record, @Param("example") PluginFrontScriptExample example);

    int updateByExample(@Param("record") PluginFrontScript record, @Param("example") PluginFrontScriptExample example);

    int updateByPrimaryKeySelective(PluginFrontScript record);

    int updateByPrimaryKeyWithBLOBs(PluginFrontScript record);
}