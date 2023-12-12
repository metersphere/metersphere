package io.metersphere.system.dto.table;

import io.metersphere.system.dto.sdk.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TableBatchProcessDTO {
    @Schema(description = "不处理的ID")
    List<String> excludeIds;

    @Schema(description = "选择的ID")
    private List<String> selectIds;

    @Schema(description = "是否选择所有数据")
    private boolean selectAll;

    @Schema(description = "查询条件")
    private BaseCondition condition = new BaseCondition();
}
