package io.metersphere.bug.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class BugTagEditDTO {

    @Schema(description = "缺陷ID")
    private String bugId;

    @Schema(description = "标签值")
    private List<String> tags;
}
