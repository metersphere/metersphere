package io.metersphere.api.dto.share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiReportShareDTO {

    @Schema(description = "分享id")
    private String id;

    @Schema(description = "分享类型 资源的类型 Single, Batch, API_SHARE_REPORT, TEST_PLAN_SHARE_REPORT")
    private String shareType;

    @Schema(description = "语言")
    private String lang;

    private String projectId;

    @Schema(description = "分享扩展数据 资源的id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportId;

    @Schema(description = "分享链接是否被删")
    private boolean deleted;

    @Schema(description = "分享链接是否过期")
    private boolean expired;
}
