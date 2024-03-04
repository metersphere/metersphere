package io.metersphere.system.dto.sdk.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class NotificationRequest extends BasePageRequest {

    @Schema(description = "ID")
    private Long id;

    @Schema(description =  "通知类型")
    private String type;

    @Schema(description =  "接收人")
    private String receiver;

    @Schema(description =  "标题")
    private String title;

    @Schema(description =  "状态")
    private String status;

    @Schema(description =  "资源类型: TEST_PLAN/BUG/CASE/API/UI/LOAD/JENKINS/SCHEDULE")
    private String resourceType;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "操作人")
    private String operator;

    @Schema(description =  "操作")
    private String operation;

}
