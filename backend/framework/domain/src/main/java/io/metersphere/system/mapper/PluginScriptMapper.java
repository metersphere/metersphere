package io.metersphere.system.mapper;

import io.metersphere.system.domain.PluginScript;
import io.metersphere.system.domain.PluginScriptExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PluginScriptMapper {
    long countByExample(PluginScriptExample example);

    int deleteByExample(PluginScriptExample example);

    int deleteByPrimaryKey(@Param("pluginId") String pluginId, @Param("scriptId") String scriptId);

    int insert(PluginScript record);

    int insertSelective(PluginScript record);

    List<PluginScript> selectByExampleWithBLOBs(PluginScriptExample example);

    List<PluginScript> selectByExample(PluginScriptExample example);

    PluginScript selectByPrimaryKey(@Param("pluginId") String pluginId, @Param("scriptId") String scriptId);

    int updateByExampleSelective(@Param("record") PluginScript record, @Param("example") PluginScriptExample example);

    int updateByExampleWithBLOBs(@Param("record") PluginScript record, @Param("example") PluginScriptExample example);

    int updateByExample(@Param("record") PluginScript record, @Param("example") PluginScriptExample example);

    int updateByPrimaryKeySelective(PluginScript record);

    int updateByPrimaryKeyWithBLOBs(PluginScript record);

    int updateByPrimaryKey(PluginScript record);

    int batchInsert(@Param("list") List<PluginScript> list);

    int batchInsertSelective(@Param("list") List<PluginScript> list, @Param("selective") PluginScript.Column ... selective);
}