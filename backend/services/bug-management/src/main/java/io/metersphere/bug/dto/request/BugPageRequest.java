package io.metersphere.bug.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BugPageRequest extends BasePageRequest {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.project_id.not_blank}")
    private String projectId;

    @Schema(description = "是否回收站, 后台默认设置")
    private boolean useTrash;

    @Schema(description = "待办参数: 后台默认设置")
    private BugTodoRequest todoParam;

    @Schema(description = "工作台参数: 是否属于测试计划")
    private Boolean relatedToPlan = false;

    @Schema(description = "工作台参数: 是否我创建的")
    private Boolean createByMe = false;

    @Schema(description = "工作台参数: 是否待我处理的")
    private Boolean assignedToMe = false;

    @Schema(description = "工作台参数: 缺陷数")
    private Boolean boardCount = false;

    @Schema(description = "工作台参数: 是否遗留的")
    private Boolean unresolved = false;
}
