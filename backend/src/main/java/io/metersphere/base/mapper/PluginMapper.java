package io.metersphere.base.mapper;

import io.metersphere.base.domain.Plugin;
import io.metersphere.base.domain.PluginExample;
import io.metersphere.base.domain.PluginWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PluginMapper {
    long countByExample(PluginExample example);

    int deleteByExample(PluginExample example);

    int deleteByPrimaryKey(String id);

    int insert(PluginWithBLOBs record);

    int insertSelective(PluginWithBLOBs record);

    List<PluginWithBLOBs> selectByExampleWithBLOBs(PluginExample example);

    List<Plugin> selectByExample(PluginExample example);

    PluginWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PluginWithBLOBs record, @Param("example") PluginExample example);

    int updateByExampleWithBLOBs(@Param("record") PluginWithBLOBs record, @Param("example") PluginExample example);

    int updateByExample(@Param("record") Plugin record, @Param("example") PluginExample example);

    int updateByPrimaryKeySelective(PluginWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(PluginWithBLOBs record);

    int updateByPrimaryKey(Plugin record);
}