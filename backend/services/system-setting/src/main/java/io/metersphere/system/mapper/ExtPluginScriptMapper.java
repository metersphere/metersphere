package io.metersphere.system.mapper;

import io.metersphere.system.domain.PluginScript;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPluginScriptMapper {
    List<PluginScript> getOptionByPluginIds(@Param("pluginIds") List<String> pluginIds);
}
