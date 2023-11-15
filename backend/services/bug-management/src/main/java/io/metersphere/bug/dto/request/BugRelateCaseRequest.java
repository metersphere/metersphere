package io.metersphere.bug.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugRelateCaseRequest extends BugRelateCasePageRequest{

    @Schema(description = "是否全选", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean selectAll;

    @Schema(description = "用例ID勾选列表")
    private List<String> includeCaseIds;
}
