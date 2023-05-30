package io.metersphere.issue.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class IssueFollow implements Serializable {
    @Schema(title = "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issue_follow.issue_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{issue_follow.issue_id.length_range}", groups = {Created.class, Updated.class})
    private String issueId;

    @Schema(title = "关注人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issue_follow.follow_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{issue_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    private String followId;

    private static final long serialVersionUID = 1L;
}