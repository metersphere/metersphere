package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author jianxing
 */
@Data
@Schema(title = "权限设置菜单项")
public class PermissionSettingDTO {
    @Schema(title = "菜单项ID")
    private String id;
    @Schema(title = "菜单项名称")
    private String name;
    @Schema(title = "是否是企业版菜单")
    private Boolean license = false;
    @Schema(title = "菜单下的权限列表")
    private List<PermissionDTO> permissions;
    @Schema(title = "子菜单")
    private List<PermissionSettingDTO> children;
}
