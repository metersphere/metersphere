package io.metersphere.system.mapper;

import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.system.dto.PluginDTO;

import java.util.List;

public interface ExtPluginMapper {
    List<PluginDTO> getPlugins();

    List<OptionDTO> selectPluginOptions(List<String> pluginIds);
}
