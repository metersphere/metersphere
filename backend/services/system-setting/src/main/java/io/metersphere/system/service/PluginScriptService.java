package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.PluginScript;
import io.metersphere.system.domain.PluginScriptExample;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.ExtPluginScriptMapper;
import io.metersphere.system.mapper.PluginScriptMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.metersphere.system.controller.result.SystemResultCode.PLUGIN_SCRIPT_EXIST;
import static io.metersphere.system.controller.result.SystemResultCode.PLUGIN_SCRIPT_FORMAT;

@Service
@Transactional(rollbackFor = Exception.class)
public class PluginScriptService {

    @Resource
    private PluginScriptMapper pluginScriptMapper;
    @Resource
    private ExtPluginScriptMapper extPluginScriptMapper;

    public void add(String pluginId, List<String> frontendScript) {
        if (CollectionUtils.isEmpty(frontendScript)) {
            return;
        }
        Set<String> ids = new HashSet<>();
        List<PluginScript> pluginScripts = new ArrayList<>(frontendScript.size());
        for (String script : frontendScript) {
            PluginScript pluginScript = new PluginScript();
            OptionDTO scriptInfo;
            try {
                scriptInfo = JSON.parseObject(script, OptionDTO.class);
            } catch (Exception e) {
                throw new MSException(PLUGIN_SCRIPT_FORMAT);
            }
            // ID 判重
            if (ids.contains(scriptInfo.getId())) {
                throw new MSException(PLUGIN_SCRIPT_EXIST);
            }
            ids.add(scriptInfo.getId());
            pluginScript.setPluginId(pluginId);
            pluginScript.setScriptId(
                    StringUtils.isBlank(scriptInfo.getId()) ? IDGenerator.nextStr() : scriptInfo.getId()
            );
            pluginScript.setName(scriptInfo.getName());
            pluginScript.setScript(script.getBytes());
            pluginScripts.add(pluginScript);
        }
        pluginScriptMapper.batchInsert(pluginScripts);
    }

    public void deleteByPluginId(String pluginId) {
        PluginScriptExample example = new PluginScriptExample();
        example.createCriteria().andPluginIdEqualTo(pluginId);
        pluginScriptMapper.deleteByExample(example);
    }

    public String getScriptContent(String pluginId, String scriptId) {
        PluginScript frontScript = get(pluginId, scriptId);
        return frontScript == null ? null : new String(frontScript.getScript());
    }

    public PluginScript get(String pluginId, String scriptId) {
        return pluginScriptMapper.selectByPrimaryKey(pluginId, scriptId);
    }

    public Map<String, List<OptionDTO>> getScripteMap(List<String> pluginIds) {
        if (CollectionUtils.isEmpty(pluginIds)) {
            return Collections.emptyMap();
        }
        List<PluginScript> scripts = extPluginScriptMapper.getOptionByPluginIds(pluginIds);
        Map<String, List<OptionDTO>> scriptMap = new HashMap<>();
        for (PluginScript script : scripts) {
            List<OptionDTO> scriptList = scriptMap.computeIfAbsent(script.getPluginId(), k -> new ArrayList<>());
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(script.getScriptId());
            optionDTO.setName(script.getName());
            scriptList.add(optionDTO);
        }
        return scriptMap;
    }
}
