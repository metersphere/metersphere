package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseFollow implements Serializable {
    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_follow.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_follow.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(title = "关注人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_follow.follow_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    private String followId;

    private static final long serialVersionUID = 1L;
}