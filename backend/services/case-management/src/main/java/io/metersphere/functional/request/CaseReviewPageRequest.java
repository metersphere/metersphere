package io.metersphere.functional.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CaseReviewPageRequest extends BasePageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "我评审的")
    private String reviewByMe;

    @Schema(description = "我创建的")
    private String createByMe;

    @Schema(description = "是否我的待办, 默认查询全部")
    private boolean myTodo = false;

    @Schema(description = "我的待办用户ID, 组合使用: myTodo=true, myTodoUserId=xxx")
    private String myTodoUserId;

}
