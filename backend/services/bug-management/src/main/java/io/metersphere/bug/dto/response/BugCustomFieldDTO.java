package io.metersphere.bug.dto.response;

import io.metersphere.system.domain.CustomField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author song-cc-rock
 */
@Data
public class BugCustomFieldDTO extends CustomField {

    @Schema(description = "字段值")
    private String value;

    @Schema(description = "缺陷ID")
    private String bugId;
}
