package io.metersphere.system.mapper;

import io.metersphere.system.dto.PluginDTO;

import java.util.List;

public interface ExtPluginMapper {
    List<PluginDTO> getPlugins();
}
