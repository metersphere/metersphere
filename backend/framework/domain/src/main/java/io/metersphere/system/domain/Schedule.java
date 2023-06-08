package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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

    @Schema(title = "cron 表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.value.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{schedule.value.length_range}", groups = {Created.class, Updated.class})
    private String value;

    @Schema(title = "Schedule Job Class Name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.job.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{schedule.job.length_range}", groups = {Created.class, Updated.class})
    private String job;

    @Schema(title = "是否开启")
    private Boolean enable;

    @Schema(title = "资源ID，api_scenario ui_scenario load_test")
    private String resourceId;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "项目ID")
    private String projectId;

    @Schema(title = "名称")
    private String name;

    @Schema(title = "配置")
    private String config;

    private static final long serialVersionUID = 1L;
}