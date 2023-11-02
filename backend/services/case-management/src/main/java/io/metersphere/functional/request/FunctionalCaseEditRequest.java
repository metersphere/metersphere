package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalCaseEditRequest extends FunctionalCaseAddRequest {

    @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String id;

    @Schema(description = "取消关联id")
    private List<String> deleteFileMetaIds;
}
