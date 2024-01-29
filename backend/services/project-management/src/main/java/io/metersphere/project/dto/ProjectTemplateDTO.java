package io.metersphere.project.dto;

import io.metersphere.system.domain.Template;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-19  16:46
 */
@Getter
@Setter
public class ProjectTemplateDTO extends Template implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否是默认模板")
    private Boolean enableDefault = false;

    @Schema(description = "是否是平台自动获取模板")
    private Boolean enablePlatformDefault = false;
}
