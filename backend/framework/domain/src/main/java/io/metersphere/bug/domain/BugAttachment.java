package io.metersphere.bug.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class BugAttachment implements Serializable {
    @Schema(description =  "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_attachment.bug_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug_attachment.bug_id.length_range}", groups = {Created.class, Updated.class})
    private String bugId;

    @Schema(description =  "文件的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_attachment.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug_attachment.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    private static final long serialVersionUID = 1L;
}