package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleRelation implements Serializable {
    @Schema(title = "用户组关系ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    @Schema(title = "组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.role_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.role_id.length_range}", groups = {Created.class, Updated.class})
    private String roleId;

    @Schema(title = "组织或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    private static final long serialVersionUID = 1L;
}