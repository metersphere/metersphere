package io.metersphere.functional.dto;

import io.metersphere.functional.domain.CaseReviewUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CaseReviewUserDTO extends CaseReviewUser {

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "头像")
    private String avatar;
}
