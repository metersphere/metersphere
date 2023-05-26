package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class OperatingLog implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operating_log.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{operating_log.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operating_log.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{operating_log.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "operating method")
    private String operMethod;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "操作人")
    private String operUser;

    @Schema(title = "资源ID")
    private String sourceId;

    @Schema(title = "操作类型")
    private String operType;

    @Schema(title = "操作模块")
    private String operModule;

    @Schema(title = "操作标题")
    private String operTitle;

    @Schema(title = "操作路径")
    private String operPath;

    @Schema(title = "操作时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operating_log.oper_time.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 19, message = "{operating_log.oper_time.length_range}", groups = {Created.class, Updated.class})
    private Long operTime;

    @Schema(title = "操作内容")
    private byte[] operContent;

    @Schema(title = "操作参数")
    private byte[] operParams;

    private static final long serialVersionUID = 1L;
}