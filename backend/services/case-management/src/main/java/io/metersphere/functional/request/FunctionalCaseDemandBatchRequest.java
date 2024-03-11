package io.metersphere.functional.request;

import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.metersphere.functional.dto.DemandDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class FunctionalCaseDemandBatchRequest extends BaseFunctionalCaseBatchDTO {

    @Schema(description = "项目ID")
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "需求所属平台(本地创建为系统平台名称)")
    @NotBlank(message = "{functional_case_demand.demand_platform.not_blank}")
    private String demandPlatform;

    @Schema(description = "需求集合(全选时可为空)")
    private List<DemandDTO> demandList;

    @Schema(description = "全选需求时的过滤条件")
    private FunctionalDemandBatchRequest functionalDemandBatchRequest;


}
