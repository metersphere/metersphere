package io.metersphere.functional.dto;

import io.metersphere.functional.domain.FunctionalCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseDTO extends FunctionalCase {

    @Schema(description =  "评论@的人, 多个以';'隔开")
    private String relatedUsers;

}
