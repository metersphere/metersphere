package io.metersphere.system.dto.table;

import io.metersphere.sdk.dto.BaseCondition;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableBatchProcessDTO {
    @Schema(description = "不处理的ID")
    List<String> excludeIds = new ArrayList<>();

    @Schema(description = "选择的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<
            @NotBlank(message = "{id must not be blank}", groups = {Created.class, Updated.class})
                    String
            > selectIds = new ArrayList<>();

    @Schema(description = "是否选择所有数据")
    private boolean selectAll;

    @Schema(description = "查询条件")
    private BaseCondition condition = new BaseCondition();
}
