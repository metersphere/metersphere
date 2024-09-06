package io.metersphere.system.controller.param;

import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.valid.EnumValue;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * UserRoleUpdateRequest 约束定义
 * @author jianxing
 */
@Data
public class UserRoleUpdateRequestDefinition {
    @NotBlank(groups = {Updated.class})
    @Size(min = 1, max = 50, groups = {Created.class, Updated.class})
    private String id;
    
    @NotBlank(groups = {Created.class})
    @Size(min = 1, max = 255, groups = {Created.class, Updated.class})
    private String name;

    @NotBlank(groups = {Created.class})
    @EnumValue(enumClass = UserRoleType.class, groups = {Created.class, Updated.class})
    @Size(min = 1, max = 20, groups = {Created.class, Updated.class})
    private String type;
}
