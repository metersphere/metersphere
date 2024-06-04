package io.metersphere.functional.dto;

import io.metersphere.sdk.constants.ModuleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author guoyuqi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinderOptionDTO {

    @Schema(description = "节点ID(用例/模块的id)")
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String id;

    @Schema(description = "节点类型")
    @NotBlank(message = "{functional_case_test.test_type.not_blank}")
    private String type = ModuleConstants.ROOT_NODE_PARENT_ID;

}
