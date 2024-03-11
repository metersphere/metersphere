package io.metersphere.functional.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class FunctionalCaseCommentRequest {

    @Schema(description = "评论ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.id.not_blank}", groups = {Updated.class})
    private String id;

    @Schema(description = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.case_id.not_blank}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description = "评论@的人的Id, 多个以';'隔开")
    private String notifier;

    @Schema(description = "回复人")
    private String replyUser;

    @Schema(description = "父评论ID")
    private String parentId;

    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.content.not_blank}", groups = {Created.class})
    private String content;

    @Schema(description = "任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.event.not_blank}", groups = {Created.class})
    private String event;

    @Schema(description = "项目Id")
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds;

}
