package io.metersphere.functional.request;

import io.metersphere.functional.dto.DemandDTO;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class FunctionalCaseDemandRequest {

    @Schema(description = "功能用例需求关系ID")
    @NotBlank(message = "{functional_case_demand.case_id.not_blank}", groups = {Updated.class})
    private String id;

    @Schema(description = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_demand.case_id.not_blank}")
    private String caseId;

    @Schema(description = "需求所属平台(本地创建为系统平台名称)")
    @NotBlank(message = "{functional_case_demand.demand_platform.not_blank}")
    private String demandPlatform;

    @Schema(description = "需求集合")
    private List<DemandDTO> demandList;

    @Schema(description = "全选需求时的过滤条件")
    private FunctionalDemandBatchRequest functionalDemandBatchRequest;

}
