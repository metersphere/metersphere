package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "评审和评审人中间表")
@Table("case_review_user")
@Data
public class CaseReviewUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{case_review_user.review_id.not_blank}", groups = {Updated.class})
    @Schema(title = "评审ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewId;

    @NotBlank(message = "{case_review_user.user_id.not_blank}", groups = {Updated.class})
    @Schema(title = "评审人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;


}