package io.metersphere.system.dto;

import io.metersphere.sdk.dto.OrganizationOptionDTO;
import io.metersphere.system.domain.Plugin;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PluginDTO extends Plugin implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(title = "插件前端表单配置项列表")
    private List<PluginDTO.PluginForm> pluginForms;

    @Schema(title = "关联的组织列表")
    private List<OrganizationOptionDTO> organizations;

    @Data
    class PluginForm {
        private String id;
        private String name;
    }
}