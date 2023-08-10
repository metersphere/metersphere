package io.metersphere.bug.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class BugFollower implements Serializable {
    @Schema(description =  "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_follower.bug_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug_follower.bug_id.length_range}", groups = {Created.class, Updated.class})
    private String bugId;

    @Schema(description =  "关注人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_follower.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug_follower.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    private static final long serialVersionUID = 1L;
}