package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "用例评审和功能用例的中间表")
@Table("case_review_functional_case")
@Data
public class CaseReviewFunctionalCase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{case_review_functional_case.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{case_review_functional_case.review_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case.review_id.not_blank}", groups = {Created.class})
    @Schema(title = "评审ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String reviewId;

    @Size(min = 1, max = 50, message = "{case_review_functional_case.case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case.case_id.not_blank}", groups = {Created.class})
    @Schema(title = "用例ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String caseId;

    @Size(min = 1, max = 64, message = "{case_review_functional_case.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case.status.not_blank}", groups = {Created.class})
    @Schema(title = "评审状态：进行中/通过/不通过/重新提审", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 64]")
    private String status;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;


    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{case_review_functional_case.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;


    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;

    @Size(min = 1, max = 1, message = "{case_review_functional_case.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case.deleted.not_blank}", groups = {Created.class})
    @Schema(title = "关联的用例是否放入回收站", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean deleted;


}