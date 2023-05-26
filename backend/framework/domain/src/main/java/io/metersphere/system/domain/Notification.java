package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class Notification implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 19, message = "{notification.id.length_range}", groups = {Created.class, Updated.class})
    private Long id;

    @Schema(title = "通知类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{notification.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "接收人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.receiver.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.receiver.length_range}", groups = {Created.class, Updated.class})
    private String receiver;

    @Schema(title = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.title.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{notification.title.length_range}", groups = {Created.class, Updated.class})
    private String title;

    @Schema(title = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{notification.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "操作人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.operator.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.operator.length_range}", groups = {Created.class, Updated.class})
    private String operator;

    @Schema(title = "操作", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.operation.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.operation.length_range}", groups = {Created.class, Updated.class})
    private String operation;

    @Schema(title = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(title = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.resource_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.resource_type.length_range}", groups = {Created.class, Updated.class})
    private String resourceType;

    @Schema(title = "资源名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.resource_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{notification.resource_name.length_range}", groups = {Created.class, Updated.class})
    private String resourceName;

    private static final long serialVersionUID = 1L;
}