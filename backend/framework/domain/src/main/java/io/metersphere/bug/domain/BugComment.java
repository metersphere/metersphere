package io.metersphere.bug.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class BugComment implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_comment.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 64, message = "{bug_comment.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_comment.bug_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{bug_comment.bug_id.length_range}", groups = {Created.class, Updated.class})
    private String bugId;

    @Schema(description =  "评论人")
    private String createUser;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_comment.description.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 65535, message = "{bug_comment.description.length_range}", groups = {Created.class, Updated.class})
    private String description;

    private static final long serialVersionUID = 1L;
}