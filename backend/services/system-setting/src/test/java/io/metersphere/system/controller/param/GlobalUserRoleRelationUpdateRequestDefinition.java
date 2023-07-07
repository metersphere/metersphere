package io.metersphere.system.controller.param;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author jianxing
 */
@Data
public class GlobalUserRoleRelationUpdateRequestDefinition {

    @NotBlank(groups = {Created.class})
    @Size(min = 1, max = 50, groups = {Created.class, Updated.class})
    private String userId;

    @NotBlank(groups = {Created.class})
    @Size(min = 1, max = 50, groups = {Created.class, Updated.class})
    private String roleId;
}
