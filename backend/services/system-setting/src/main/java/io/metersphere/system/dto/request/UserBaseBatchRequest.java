package io.metersphere.system.dto.request;

import io.metersphere.system.dto.request.user.BaseCondition;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserBaseBatchRequest {
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<
            @NotBlank(message = "{user_role_relation.user_id.not_blank}", groups = {Created.class, Updated.class})
            @Size(min = 1, max = 50, message = "{user_role_relation.user_id.length_range}", groups = {Created.class, Updated.class})
                    String
            > userIds;

    @Schema(description = "不处理的用户ID")
    List<String> skipIds;

    @Schema(description = "是否选择所有数据")
    private boolean selectAll;

    @Schema(description = "查询条件")
    private BaseCondition condition = new BaseCondition();
}
