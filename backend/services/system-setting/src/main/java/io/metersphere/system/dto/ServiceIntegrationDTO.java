package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ServiceIntegrationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description =  "ID")
    private String id;

    @Schema(description =  "插件的ID")
    private String pluginId;

    @Schema(description =  "插件的名称")
    private String title;

    @Schema(description =  "插件描述")
    private String description;

    @Schema(description =  "是否启用")
    private Boolean enable;

    @Schema(description =  "是否配置")
    private Boolean config;

    @Schema(description =  "logo图片地址")
    private String logo;

    @Schema(description =  "组织ID")
    private String organizationId;

    @Schema(description =  "配置的表单值，已配置才有值")
    private Map<String, String> configuration;
}
