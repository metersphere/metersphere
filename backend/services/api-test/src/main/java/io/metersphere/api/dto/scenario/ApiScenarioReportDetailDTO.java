package io.metersphere.api.dto.scenario;

import io.metersphere.sdk.dto.api.result.RequestResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiScenarioReportDetailDTO {

    @Schema(description = "报告详情id")
    private String id;

    @Schema(description = "接口报告id")
    private String reportId;

    @Schema(description = "各个步骤请求唯一标识")
    private String stepId;

    @Schema(description = "结果状态")
    private String status;

    @Schema(description = "误报编号/误报状态独有")
    private String fakeCode;

    @Schema(description = "请求名称")
    private String requestName;

    @Schema(description = "请求耗时")
    private Long requestTime;

    @Schema(description = "请求响应码")
    private String code;

    @Schema(description = "响应内容大小")
    private Long responseSize;

    @Schema(description = "脚本标识")
    private String scriptIdentifier;

    @Schema(description = "结果内容详情")
    private RequestResult content;


}
