package io.metersphere.system.dto;

import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.UserRole;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserInfo {

    @Schema(title = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user.id.length_range}", groups = {Updated.class})
    private String id;

    @Schema(title = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.email.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{user.email.length_range}", groups = {Created.class, Updated.class})
    @Email(message = "{user.email.invalid}", groups = {Created.class, Updated.class})
    private String email;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "手机号")
    private String phone;

    @Schema(title = "来源：LOCAL OIDC CAS OAUTH2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.source.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user.source.length_range}", groups = {Created.class, Updated.class})
    private String source;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "修改人")
    private String updateUser;

    @Schema(title = "用户所属组织")
    private List<Organization> organizationList = new ArrayList<>();
    @Schema(title = "用户所属用户组")
    private List<UserRole> userRoleList = new ArrayList<>();
}
