package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 权限信息
 * @author jianxing
 */
@Data
@Schema(title = "权限信息")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(title = "权限ID")
    private String id;
    @Schema(title = "权限名称")
    private String name;
    @Schema(title = "是否启用该权限")
    private Boolean enable = false;
    @Schema(title = "是否是企业权限")
    private Boolean license = false;
}
