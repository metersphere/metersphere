package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class UserRoleRelation implements Serializable {
    @Schema(title = "用户组关系ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.id.not_blank}", groups = {Updated.class})
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

    @Schema(title = "工作空间或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}