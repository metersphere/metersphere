package io.metersphere.system.dto;

import io.metersphere.system.domain.Plugin;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PluginDTO extends Plugin implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description =  "插件前端表单配置项列表")
    private List<OptionDTO> pluginForms;

    @Schema(description =  "关联的组织列表")
    private List<OptionDTO> organizations;
}