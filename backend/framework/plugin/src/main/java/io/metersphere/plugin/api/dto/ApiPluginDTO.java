package io.metersphere.plugin.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiPluginDTO {
    /**
     * jar唯一标识且不变，每次上传jar通过它更新
     */
    private String pluginId;
    /**
     * UI脚本，一个UI步骤一条
     */
    private List<ScriptDTO> uiScripts;

    public ApiPluginDTO() {
    }

    public ApiPluginDTO(String pluginId, List<ScriptDTO> uiScripts) {
        this.pluginId = pluginId;
        this.uiScripts = uiScripts;
    }
}
