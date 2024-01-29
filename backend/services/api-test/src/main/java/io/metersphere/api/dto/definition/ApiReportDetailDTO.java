package io.metersphere.api.dto.definition;

import io.metersphere.sdk.dto.api.result.RequestResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiReportDetailDTO {

    /**
     * 报告详情id
     */
    @Schema(description = "报告详情id")
    private String id;

    /**
     * 接口报告id
     */
    @Schema(description = "接口报告id")
    private String reportId;

    /**
     * 各个步骤请求唯一标识
     */
    @Schema(description = "各个步骤请求唯一标识")
    private String stepId;

    /**
     * 结果状态
     */
    @Schema(description = "结果状态")
    private String status;

    /**
     * 误报编号/误报状态独有
     */
    @Schema(description = "误报编号/误报状态独有")
    private String fakeCode;

    /**
     * 请求名称
     */
    @Schema(description = "请求名称")
    private String requestName;

    /**
     * 请求耗时
     */
    @Schema(description = "请求耗时")
    private Long requestTime;

    /**
     * 请求响应码
     */
    @Schema(description = "请求响应码")
    private String code;

    /**
     * 响应内容大小
     */
    @Schema(description = "响应内容大小")
    private Long responseSize;

    /**
     * 脚本标识
     */
    @Schema(description = "脚本标识")
    private String scriptIdentifier;

    /**
     * 结果内容详情
     */
    @Schema(description = "结果内容详情")
    private RequestResult content;


}
