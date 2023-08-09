package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ServiceIntegrationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(title = "ID")
    private String id;

    @Schema(title = "插件的ID")
    private String pluginId;

    @Schema(title = "插件的名称")
    private String title;

    @Schema(title = "插件描述")
    private String describe;

    @Schema(title = "是否启用")
    private Boolean enable;

    @Schema(title = "是否配置")
    private Boolean config;

    @Schema(title = "logo图片地址")
    private String logo;

    @Schema(title = "组织ID")
    private String organizationId;

    @Schema(title = "配置的表单值，已配置才有值")
    private Map<String, String> configuration;
}
