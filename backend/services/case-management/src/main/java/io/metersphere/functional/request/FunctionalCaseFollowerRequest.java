package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalCaseFollowerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户id")
    @NotBlank(message = "{user_id.not_blank}")
    private String userId;

    @Schema(description = "用例id")
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String functionalCaseId;

}
