package io.metersphere.bug.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class BugBlob implements Serializable {
    @Schema(title = "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{bug_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "缺陷描述")
    private String description;

    private static final long serialVersionUID = 1L;
}