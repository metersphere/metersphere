package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限信息
 * @author jianxing
 */
@Data
@Schema(title = "权限信息")
public class PermissionDTO {
    @Schema(title = "权限ID")
    private String id;
    @Schema(title = "权限名称")
    private String name;
    @Schema(title = "权限所属菜单ID")
    private String resourceId;
    @Schema(title = "是否启用该权限")
    private Boolean enable = false;
    @Schema(title = "是否是企业权限")
    private Boolean license = false;
}
