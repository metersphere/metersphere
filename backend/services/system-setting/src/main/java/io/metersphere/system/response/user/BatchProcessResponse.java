package io.metersphere.system.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BatchProcessResponse {
    @Schema(description = "全部数量")
    private long totalCount;
    @Schema(description = "成功数量")
    private long successCount;
}
