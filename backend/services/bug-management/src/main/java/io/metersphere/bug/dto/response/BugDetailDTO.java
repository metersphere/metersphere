package io.metersphere.bug.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BugDetailDTO extends BugDTO {

    @Schema(description = "附件集合")
    List<BugFileDTO> attachments;
}
