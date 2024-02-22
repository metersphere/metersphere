package io.metersphere.api.dto.definition;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExecutePageRequest extends BasePageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例id/场景id")
    @NotBlank(message = "{api_test_case.id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_test_case.id.length_range}")
    private String id;

}
