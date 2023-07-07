package io.metersphere.system.controller.param;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * PermissionSettingUpdateRequest 约束定义
 * 基于该定义生成非法参数进行参数校验
 * 相对 PermissionSettingUpdateRequest 不需要写 swagger 的注解，也不用写 massage
 * 有继承通用的参数的话，可以单独针对通用参数写一次校验就行，就不继承了，例如 BasePageRequestDefinition
 * @author jianxing
 */
@Data
public class PermissionSettingUpdateRequestDefinition {
    @NotBlank
    private String userRoleId;
    @NotNull
    @Valid
    private List<PermissionUpdateRequest> permissions;

    @Data
    public static class PermissionUpdateRequest {
        @NotBlank
        private String id;
    }
}
