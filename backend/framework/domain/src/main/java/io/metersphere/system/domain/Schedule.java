package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class Schedule implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{schedule.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "qrtz UUID")
    private String key;

    @Schema(title = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{schedule.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "Schedule value", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.value.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{schedule.value.length_range}", groups = {Created.class, Updated.class})
    private String value;

    @Schema(title = "Schedule Job Class Name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.job.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{schedule.job.length_range}", groups = {Created.class, Updated.class})
    private String job;

    @Schema(title = "Schedule Eable")
    private Boolean enable;

    @Schema(title = "")
    private String resourceId;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{schedule.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "Create timestamp")
    private Long createTime;

    @Schema(title = "Update timestamp")
    private Long updateTime;

    @Schema(title = "项目ID")
    private String projectId;

    @Schema(title = "名称")
    private String name;

    @Schema(title = "配置")
    private String config;

    private static final long serialVersionUID = 1L;
}