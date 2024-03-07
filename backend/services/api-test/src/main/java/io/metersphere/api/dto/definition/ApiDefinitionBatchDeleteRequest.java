package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionBatchDeleteRequest extends ApiDefinitionBatchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "删除列表版本/删除全部版本")
    private Boolean deleteAllVersion = false;
}
