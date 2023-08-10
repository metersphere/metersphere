package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class OperationLog implements Serializable {
    @Schema(description =  "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{operation_log.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{operation_log.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "组织id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{operation_log.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description =  "操作时间")
    private Long createTime;

    @Schema(description =  "操作人")
    private String createUser;

    @Schema(description =  "资源id")
    private String sourceId;

    @Schema(description =  "操作方法", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.method.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{operation_log.method.length_range}", groups = {Created.class, Updated.class})
    private String method;

    @Schema(description =  "操作类型/add/update/delete", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{operation_log.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description =  "操作模块/api/case/scenario/ui")
    private String module;

    @Schema(description =  "操作详情")
    private String content;

    @Schema(description =  "操作路径")
    private String path;

    private static final long serialVersionUID = 1L;
}