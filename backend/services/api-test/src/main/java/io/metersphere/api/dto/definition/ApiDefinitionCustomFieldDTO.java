package io.metersphere.api.dto.definition;

import io.metersphere.system.domain.CustomField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: LAN
 * @date: 2023/12/12 10:17
 * @version: 1.0
 */
@Data
public class ApiDefinitionCustomFieldDTO extends CustomField {
    @Schema(description = "字段值")
    private String value;

    @Schema(description = "接口ID")
    private String apiId;
}
