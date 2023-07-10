package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class OperationLog implements Serializable {
    @Schema(title = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{operation_log.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{operation_log.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "操作时间")
    private Long createTime;

    @Schema(title = "操作人")
    private String createUser;

    @Schema(title = "资源id")
    private String sourceId;

    @Schema(title = "操作方法", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.method.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{operation_log.method.length_range}", groups = {Created.class, Updated.class})
    private String method;

    @Schema(title = "操作类型/add/update/delete", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{operation_log.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "操作模块/api/case/scenario/ui")
    private String module;

    @Schema(title = "操作内容")
    private String content;

    @Schema(title = "操作路径")
    private String path;

    private static final long serialVersionUID = 1L;
}