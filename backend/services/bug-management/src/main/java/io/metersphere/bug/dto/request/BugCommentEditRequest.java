package io.metersphere.bug.dto.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BugCommentEditRequest implements Serializable {

    @Schema(description = "评论ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_comment.id.not_blank}", groups = {Updated.class})
    private String id;

    @Schema(description = "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bugId;

    @Schema(description = "回复人")
    private String replyUser;

    @Schema(description = "通知人, @名称展示, 以用户ID';'分隔")
    private String notifier;

    @Schema(description = "父评论ID")
    private String parentId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description =  "任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLY‘;)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_comment.event.not_blank}", groups = {Created.class})
    private String event;

    @Schema(description = "富文本临时文件ID")
    private List<String> richTextTmpFileIds;
}
