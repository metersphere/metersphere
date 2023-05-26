package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class User implements Serializable {
    @Schema(title = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{user.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.email.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{user.email.length_range}", groups = {Created.class, Updated.class})
    private String email;

    @Schema(title = "用户密码")
    private String password;

    @Schema(title = "用户状态，启用或禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "语言")
    private String language;

    @Schema(title = "当前工作空间ID")
    private String lastWorkspaceId;

    @Schema(title = "手机号")
    private String phone;

    @Schema(title = "来源：LOCAL OIDC CAS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.source.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user.source.length_range}", groups = {Created.class, Updated.class})
    private String source;

    @Schema(title = "当前项目ID")
    private String lastProjectId;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    private static final long serialVersionUID = 1L;
}