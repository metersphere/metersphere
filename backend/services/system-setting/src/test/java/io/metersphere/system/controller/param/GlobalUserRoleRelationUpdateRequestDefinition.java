package io.metersphere.system.controller.param;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author jianxing
 */
@Data
public class GlobalUserRoleRelationUpdateRequestDefinition {

    @NotEmpty(groups = {Created.class, Updated.class})
    private List<
            @NotBlank(groups = {Created.class, Updated.class})
            @Size(groups = {Created.class, Updated.class})
            String> userIds;

    @NotBlank(groups = {Created.class})
    @Size(min = 1, max = 50, groups = {Created.class, Updated.class})
    private String roleId;
}
