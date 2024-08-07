package io.metersphere.system.mapper;

import io.metersphere.system.dto.PluginDTO;
import io.metersphere.system.dto.sdk.OptionDTO;

import java.util.List;

public interface ExtPluginMapper {
    List<PluginDTO> getPlugins();

    List<OptionDTO> selectPluginOptions(List<String> pluginIds);
}
