package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestCaseFollow implements Serializable {
    @Schema(title = "用例fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case_follow.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case_follow.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(title = "关注人/用户fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case_follow.follow_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    private String followId;

    private static final long serialVersionUID = 1L;
}