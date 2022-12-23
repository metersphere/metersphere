package io.metersphere.xpack.license.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LicenseInfoDTO implements Serializable {
    /**
     * 客户名称
     */
    private String corporation;
    /**
     * 授权截止时间
     */
    private String expired;
    /**
     * 产品名称
     */
    private String product;
    /**
     * 产品版本
     */
    private String edition;
    /**
     * icense版本
     */
    private String licenseVersion;
    /**
     * 授权数量
     */
    private int count;
    /**
     * 序列号
     */
    private String serialNo;
    /**
     * 备注
     */
    private String remark;
}
