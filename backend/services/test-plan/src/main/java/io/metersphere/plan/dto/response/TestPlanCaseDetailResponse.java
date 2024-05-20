package io.metersphere.plan.dto.response;

import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class TestPlanCaseDetailResponse extends FunctionalCaseDetailDTO {

    @Schema(description = "用例缺陷列表总数量")
    private Integer bugListCount;

    @Schema(description = "用例执行历史总数量")
    private Integer runListCount;
}
