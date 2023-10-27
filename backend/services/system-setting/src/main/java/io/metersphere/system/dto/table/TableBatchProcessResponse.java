package io.metersphere.system.dto.table;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableBatchProcessResponse {
    @Schema(description = "全部数量")
    private long totalCount;
    @Schema(description = "成功数量")
    private long successCount;
}
