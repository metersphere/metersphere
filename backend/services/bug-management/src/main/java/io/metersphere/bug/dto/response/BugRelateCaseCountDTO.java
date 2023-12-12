package io.metersphere.bug.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author song-cc-rock
 */
@Data
public class BugRelateCaseCountDTO {

    @Schema(description = "缺陷ID")
    private String bugId;

    @Schema(description = "关联用例数量")
    private Integer relationCaseCount;
}
