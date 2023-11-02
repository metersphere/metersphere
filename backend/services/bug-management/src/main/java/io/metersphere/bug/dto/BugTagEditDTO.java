package io.metersphere.bug.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugTagEditDTO {

    @Schema(description = "缺陷ID")
    private String bugId;

    @Schema(description = "标签值")
    private String tag;
}
