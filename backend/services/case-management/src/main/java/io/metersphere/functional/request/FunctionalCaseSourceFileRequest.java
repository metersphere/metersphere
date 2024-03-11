package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class FunctionalCaseSourceFileRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

    @Schema(description = "用例id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_functional_case.case_id.not_blank}")
    private String caseId;

    @Schema(description = "文件来源:附件(ATTACHMENT)/功能用例详情(CASE_DETAIL)/用例评论(CASE_COMMENT)/评审评论(REVIEW_COMMENT)")
    @NotBlank(message = "{functional_case.file_source.not_blank}")
    private String fileSource;

    @Schema(description = "是否本地",requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean local;


}
