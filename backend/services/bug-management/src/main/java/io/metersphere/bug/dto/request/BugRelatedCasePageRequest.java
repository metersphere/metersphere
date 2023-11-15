package io.metersphere.bug.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugRelatedCasePageRequest extends BasePageRequest {

    @Schema(description = "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bugId;
}
