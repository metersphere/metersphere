package io.metersphere.functional.request;

import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalCaseCommentRequest {

    @Schema(description =  "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.case_id.not_blank}", groups = {Created.class})
    private String caseId;

    @Schema(description =  "评论@的人, 多个以';'隔开")
    private String  notifier;

    @Schema(description =  "回复人")
    private String  replyUser;

    @Schema(description =  "父评论ID")
    private String parentId;

    @Schema(description =  "内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.content.not_blank}", groups = {Created.class})
    private String content;

}
