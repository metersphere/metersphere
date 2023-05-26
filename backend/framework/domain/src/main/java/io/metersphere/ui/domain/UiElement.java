package io.metersphere.ui.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiElement implements Serializable {
    @Schema(title = "元素id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "元素num", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.num.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 10, message = "{ui_element.num.length_range}", groups = {Created.class, Updated.class})
    private Integer num;

    @Schema(title = "元素所属模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.module_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(title = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{ui_element.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "定位类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.location_type.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 30, message = "{ui_element.location_type.length_range}", groups = {Created.class, Updated.class})
    private String locationType;

    @Schema(title = "元素定位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.location.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 300, message = "{ui_element.location.length_range}", groups = {Created.class, Updated.class})
    private String location;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.create_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "更新人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.update_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element.update_user.length_range}", groups = {Created.class, Updated.class})
    private String updateUser;

    @Schema(title = "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.version_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(title = "指向初始版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.ref_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.pos.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 19, message = "{ui_element.pos.length_range}", groups = {Created.class, Updated.class})
    private Long pos;

    @Schema(title = "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.latest.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{ui_element.latest.length_range}", groups = {Created.class, Updated.class})
    private Boolean latest;

    @Schema(title = "元素描述")
    private String description;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}