package io.metersphere.functional.dto;

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

    @Schema(description = "节点ID，如果是用例的子节点，比如 前置条件，步骤描述，等传其父节点ID")
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String id;

    @Schema(description = "节点类型")
    @NotBlank(message = "{functional_case_test.test_type.not_blank}")
    private String type;

    @Schema(description = "节点顺序")
    @NotBlank(message = "{functional_case.pos.not_blank}")
    private Long pos;
}
