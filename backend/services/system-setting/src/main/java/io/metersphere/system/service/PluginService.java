package io.metersphere.system.service;


import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.PluginFrontScript;
import io.metersphere.system.dto.PluginDTO;
import io.metersphere.system.dto.PluginListDTO;
import io.metersphere.system.mapper.PluginFrontScriptMapper;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.request.PluginUpdateRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author jianxing
 * @date : 2023-7-13
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PluginService {

    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private PluginFrontScriptMapper pluginFrontScriptMapper;

    public List<PluginListDTO> list() {
        return new ArrayList<>();
    }

    public PluginDTO get(String id) {
        Plugin plugin = pluginMapper.selectByPrimaryKey(id);
        PluginDTO pluginDTO = new PluginDTO();
        BeanUtils.copyBean(plugin, pluginDTO);
        return pluginDTO;
    }

    public Plugin add(PluginUpdateRequest request, MultipartFile file) {
        Plugin plugin = new Plugin();
        BeanUtils.copyBean(plugin, request);
        plugin.setId(UUID.randomUUID().toString());
        plugin.setPluginId(UUID.randomUUID().toString());
        plugin.setFileName(file.getName());
        plugin.setCreateTime(System.currentTimeMillis());
        plugin.setUpdateTime(System.currentTimeMillis());
        plugin.setXpack(false);
        pluginMapper.insert(plugin);
        return plugin;
    }

    public Plugin update(PluginUpdateRequest request, MultipartFile file) {
        Plugin plugin = new Plugin();
        BeanUtils.copyBean(plugin, request);
        plugin.setCreateTime(null);
        plugin.setUpdateTime(null);
        plugin.setCreateUser(null);
        pluginMapper.updateByPrimaryKeySelective(plugin);
        return plugin;
    }

    public String delete(String id) {
        pluginMapper.deleteByPrimaryKey(id);
        return id;
    }

    public String getScript(String pluginId, String scriptId) {
        PluginFrontScript frontScript = pluginFrontScriptMapper.selectByPrimaryKey(pluginId, scriptId);
        return frontScript == null ? null : frontScript.getScript();
    }
}