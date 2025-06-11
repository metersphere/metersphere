package io.metersphere.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiCaseAiResponse {

    @Schema(description = "成功数量")
    private int successCount = 0;

    @Schema(description = "失败数量")
    private int errorCount = 0;

    @Schema(description = "失败详情")
    private String errorDetail;


    public void incrementErrCount() {
        this.errorCount++;
    }

    public void incrementSuccessCount() {
        this.successCount++;
    }

}
