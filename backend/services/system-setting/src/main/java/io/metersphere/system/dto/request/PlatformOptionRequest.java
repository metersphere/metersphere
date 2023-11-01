package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlatformOptionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "插件id")
    private String pluginId;
    @Schema(description = "组织id")
    private String organizationId;

    @Schema(description = "方法")
    private String optionMethod;
    @Schema(description = "输入参数")
    private String projectConfig;

}
