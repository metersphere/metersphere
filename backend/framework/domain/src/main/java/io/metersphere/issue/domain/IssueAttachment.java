package io.metersphere.issue.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class IssueAttachment implements Serializable {
    @Schema(title = "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issue_attachment.issue_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{issue_attachment.issue_id.length_range}", groups = {Created.class, Updated.class})
    private String issueId;

    @Schema(title = "文件的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issue_attachment.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{issue_attachment.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    private static final long serialVersionUID = 1L;
}