package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "接口用例关注人")
@Table("api_test_case_follow")
@Data
public class ApiTestCaseFollow implements Serializable {
    private static final long serialVersionUID = 1L;
    @Size(min = 1, max = 50, message = "{api_test_case_follow.case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case_follow.case_id.not_blank}", groups = {Created.class})
    @Schema(title = "用例fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String caseId;

    @Size(min = 1, max = 50, message = "{api_test_case_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case_follow.follow_id.not_blank}", groups = {Created.class})
    @Schema(title = "关注人/用户fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String followId;

}