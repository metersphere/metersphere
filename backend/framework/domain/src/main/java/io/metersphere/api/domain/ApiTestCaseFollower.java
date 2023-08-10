package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestCaseFollower implements Serializable {
    @Schema(description =  "用例fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case_follower.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case_follower.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description =  "关注人/用户fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case_follower.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case_follower.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    private static final long serialVersionUID = 1L;
}