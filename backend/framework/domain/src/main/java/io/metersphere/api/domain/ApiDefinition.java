package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "接口定义")
@Table("api_definition")
@Data
public class ApiDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_definition.id.not_blank}", groups = {Updated.class})
    @Schema(title = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Size(min = 1, max = 50, message = "{api_definition.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;


    @Schema(title = "修改时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_definition.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.update_user.not_blank}", groups = {Created.class})
    @Schema(title = "修改人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateUser;


    @Schema(title = "删除人", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String deleteUser;


    @Schema(title = "删除时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long deleteTime;

    @Size(min = 1, max = 1, message = "{api_definition.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.deleted.not_blank}", groups = {Created.class})
    @Schema(title = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean deleted;

    @Size(min = 1, max = 255, message = "{api_definition.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.name.not_blank}", groups = {Created.class})
    @Schema(title = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(min = 1, max = 50, message = "{api_definition.method.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.method.not_blank}", groups = {Created.class})
    @Schema(title = "接口类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String method;

    @Size(min = 1, max = 20, message = "{api_definition.protocol.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.protocol.not_blank}", groups = {Created.class})
    @Schema(title = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private String protocol;


    @Schema(title = "接口路径-只有HTTP协议有值", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String path;


    @Schema(title = "模块全路径-用于导入处理", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String modulePath;

    @Size(min = 1, max = 50, message = "{api_definition.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.status.not_blank}", groups = {Created.class})
    @Schema(title = "接口状态/进行中/已完成", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;


    @Schema(title = "模块fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String moduleId;


    @Schema(title = "自定义id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer num;


    @Schema(title = "标签", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tags;


    @Schema(title = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;

    @Size(min = 1, max = 1, message = "{api_definition.sync_enable.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.sync_enable.not_blank}", groups = {Created.class})
    @Schema(title = "是否启用同步", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean syncEnable;


    @Schema(title = "同步开始时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long syncTime;

    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;


    @Schema(title = "环境fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String environmentId;

    @Size(min = 1, max = 1, message = "{api_definition.latest.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.latest.not_blank}", groups = {Created.class})
    @Schema(title = "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean latest;


    @Schema(title = "版本fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String versionId;


    @Schema(title = "版本引用fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String refId;


    @Schema(title = "描述", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;


}