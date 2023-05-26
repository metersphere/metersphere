package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinition implements Serializable {
    @Schema(title = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.create_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "修改时间")
    private Long updateTime;

    @Schema(title = "修改人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.update_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition.update_user.length_range}", groups = {Created.class, Updated.class})
    private String updateUser;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.deleted.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{api_definition.deleted.length_range}", groups = {Created.class, Updated.class})
    private Boolean deleted;

    @Schema(title = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{api_definition.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "接口类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.method.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition.method.length_range}", groups = {Created.class, Updated.class})
    private String method;

    @Schema(title = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.protocol.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 20, message = "{api_definition.protocol.length_range}", groups = {Created.class, Updated.class})
    private String protocol;

    @Schema(title = "接口路径-只有HTTP协议有值")
    private String path;

    @Schema(title = "模块全路径-用于导入处理")
    private String modulePath;

    @Schema(title = "接口状态/进行中/已完成", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.status.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "模块fk")
    private String moduleId;

    @Schema(title = "自定义id")
    private Integer num;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.pos.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 19, message = "{api_definition.pos.length_range}", groups = {Created.class, Updated.class})
    private Long pos;

    @Schema(title = "是否启用同步", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.sync_enable.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{api_definition.sync_enable.length_range}", groups = {Created.class, Updated.class})
    private Boolean syncEnable;

    @Schema(title = "同步开始时间")
    private Long syncTime;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "环境fk")
    private String environmentId;

    @Schema(title = "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.latest.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{api_definition.latest.length_range}", groups = {Created.class, Updated.class})
    private Boolean latest;

    @Schema(title = "版本fk")
    private String versionId;

    @Schema(title = "版本引用fk")
    private String refId;

    @Schema(title = "描述")
    private String description;

    private static final long serialVersionUID = 1L;
}