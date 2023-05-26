package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UserRole implements Serializable {
    @Schema(title = "组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_role.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user_role.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "是否是系统用户组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.system.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{user_role.system.length_range}", groups = {Created.class, Updated.class})
    private Boolean system;

    @Schema(title = "所属类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{user_role.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "创建人(操作人）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "应用范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;

    private static final long serialVersionUID = 1L;
}