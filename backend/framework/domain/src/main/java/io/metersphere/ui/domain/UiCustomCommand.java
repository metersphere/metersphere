package io.metersphere.ui.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiCustomCommand implements Serializable {
    @Schema(title = "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.module_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(title = "模块路径", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.module_path.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1000, message = "{ui_custom_command.module_path.length_range}", groups = {Created.class, Updated.class})
    private String modulePath;

    @Schema(title = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{ui_custom_command.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.level.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 100, message = "{ui_custom_command.level.length_range}", groups = {Created.class, Updated.class})
    private String level;

    @Schema(title = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.status.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 100, message = "{ui_custom_command.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "责任人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.principal.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.principal.length_range}", groups = {Created.class, Updated.class})
    private String principal;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "最后执行结果")
    private String lastResult;

    @Schema(title = "num", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.num.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 10, message = "{ui_custom_command.num.length_range}", groups = {Created.class, Updated.class})
    private Integer num;

    @Schema(title = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.deleted.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{ui_custom_command.deleted.length_range}", groups = {Created.class, Updated.class})
    private Boolean deleted;

    @Schema(title = "自定义num")
    private String customNum;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.create_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.pos.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 19, message = "{ui_custom_command.pos.length_range}", groups = {Created.class, Updated.class})
    private Long pos;

    @Schema(title = "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.version_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(title = "指向初始版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.ref_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{ui_custom_command.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(title = "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.latest.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{ui_custom_command.latest.length_range}", groups = {Created.class, Updated.class})
    private Boolean latest;

    @Schema(title = "描述")
    private String description;

    private static final long serialVersionUID = 1L;
}