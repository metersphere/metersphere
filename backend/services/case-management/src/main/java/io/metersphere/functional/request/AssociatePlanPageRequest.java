package io.metersphere.functional.request;

import io.metersphere.request.BaseProviderCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AssociatePlanPageRequest extends BaseProviderCondition {


    @Schema(description = "功能用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{associate_plan_page_request.case_id.not_blank}")
    private String caseId;


    @Min(value = 1, message = "当前页码必须大于0")
    @Schema(description =  "当前页码")
    private int current;

    @Min(value = 5, message = "每页显示条数必须不小于5")
    @Max(value = 500, message = "每页显示条数不能大于500")
    @Schema(description =  "每页显示条数")
    private int pageSize;
}
