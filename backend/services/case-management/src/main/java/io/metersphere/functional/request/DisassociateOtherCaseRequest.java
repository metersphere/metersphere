package io.metersphere.functional.request;

import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisassociateOtherCaseRequest extends BaseFunctionalCaseBatchDTO {

    @Schema(description = "功能用例选择的项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_test_case_disassociate_request.case_id.not_blank}")
    private String caseId;

    @Schema(description = "关联用例的类型(API,SCENARIO,UI,PERFORMANCE)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_test_case_disassociate_request.type.not_blank}")
    private String sourceType;


}
