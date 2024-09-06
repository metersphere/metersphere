package io.metersphere.system.dto.sdk.request;

import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.valid.EnumValue;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * @author jianxing
 */
@Data
public class UserRoleUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description =  "组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_role.id.length_range}", groups = {Created.class, Updated.class})
    private String id;
    
    @Schema(description =  "组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user_role.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "描述")
    @Size(max = 1000, groups = {Created.class, Updated.class})
    private String description;

    @Schema(description =  "所属类型 SYSTEM ORGANIZATION PROJECT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.type.not_blank}", groups = {Created.class})
    @EnumValue(enumClass = UserRoleType.class, groups = {Created.class, Updated.class})
    @Size(min = 1, max = 20, message = "{user_role.type.length_range}", groups = {Created.class, Updated.class})
    private String type;
}
