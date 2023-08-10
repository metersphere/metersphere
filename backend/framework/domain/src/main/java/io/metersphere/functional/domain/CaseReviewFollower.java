package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class CaseReviewFollower implements Serializable {
    @Schema(description =  "评审ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_follower.review_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_follower.review_id.length_range}", groups = {Created.class, Updated.class})
    private String reviewId;

    @Schema(description =  "关注人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_follower.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_follower.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    private static final long serialVersionUID = 1L;
}