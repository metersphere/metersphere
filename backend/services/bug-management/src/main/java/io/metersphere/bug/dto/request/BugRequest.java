package io.metersphere.bug.dto.request;

import io.metersphere.sdk.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author song-cc-rock
 */
@Data
public class BugRequest extends BasePageRequest {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "是否回收站")
    private Boolean useTrash;
}
