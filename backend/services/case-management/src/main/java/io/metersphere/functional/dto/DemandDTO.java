package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author guoyuqi
 */
@Data
public class DemandDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "需求ID")
    private String demandId;

    @Schema(description = "父需求ID")
    private String parent;

    @Schema(description = "需求标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_demand.demand_name.not_blank}")
    private String demandName;

    @Schema(description = "需求地址")
    private String demandUrl;
}
