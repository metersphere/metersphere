package io.metersphere.project.dto;

import io.metersphere.system.dto.sdk.OptionDTO;
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
public class ProjectTemplateOptionDTO extends OptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否是默认模板")
    private Boolean enableDefault;
}
