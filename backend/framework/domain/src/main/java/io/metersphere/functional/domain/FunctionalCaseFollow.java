package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "功能用例和关注人的中间表")
@Table("functional_case_follow")
@Data
public class FunctionalCaseFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{functional_case_follow.case_id.not_blank}", groups = {Updated.class})
    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String caseId;

    @NotBlank(message = "{functional_case_follow.follow_id.not_blank}", groups = {Updated.class})
    @Schema(title = "关注人ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String followId;


}