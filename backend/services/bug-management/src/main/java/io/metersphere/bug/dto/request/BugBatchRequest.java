package io.metersphere.bug.dto.request;

import io.metersphere.system.dto.sdk.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugBatchRequest extends BaseCondition {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "是否全选", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean selectAll;

    @Schema(description = "缺陷ID勾选集合")
    private List<String> includeBugIds;
}
