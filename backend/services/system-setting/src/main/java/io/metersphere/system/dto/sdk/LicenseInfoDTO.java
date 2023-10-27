package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class LicenseInfoDTO implements Serializable {
    /**
     * 客户名称
     */
    @Schema(description = "客户名称")
    private String corporation;
    /**
     * 授权截止时间
     */
    @Schema(description = "授权截止时间")
    private String expired;
    /**
     * 产品名称
     */
    @Schema(description = "产品名称")
    private String product;
    /**
     * 产品版本
     */
    @Schema(description = "产品版本")
    private String edition;
    /**
     * icense版本
     */
    @Schema(description = "license版本")
    private String licenseVersion;
    /**
     * 授权数量
     */
    @Schema(description = "授权数量")
    private int count;
}
