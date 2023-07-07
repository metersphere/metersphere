package io.metersphere.system.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : jianxing
 */
@Getter
@Setter
public class GlobalUserRoleRelationQueryRequestDefinition {
    @NotBlank
    private String roleId;
}
