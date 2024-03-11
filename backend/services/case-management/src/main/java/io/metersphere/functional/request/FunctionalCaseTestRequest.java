package io.metersphere.functional.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FunctionalCaseTestRequest extends BasePageRequest {

    @Schema(description = "关联关系表里主ID eg:功能用例关联接口用例时为功能用例id")
    @NotBlank(message = "{custom_field_test_case.resource_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}")
    private String sourceId;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "关联用例的类型(API,SCENARIO,UI,PERFORMANCE)")
    private String sourceType;

}
