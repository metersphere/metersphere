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

    @Schema(description = "是否回收站")
    private boolean useTrash;

    @Schema(description = "是否我的待办, 默认查询全部")
    private boolean myTodo = false;

    @Schema(description = "我的待办用户ID, 组合使用: myTodo=true, myTodoUserId=xxx")
    private String myTodoUserId;
}
