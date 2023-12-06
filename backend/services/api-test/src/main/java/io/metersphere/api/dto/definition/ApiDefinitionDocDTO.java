package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author lan
 */
@Data
public class ApiDefinitionDocDTO{

    @Schema(description = "文档标题名称")
    private String docTitle;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "接口文档内容")
    ApiDefinitionDTO docInfo;

}
