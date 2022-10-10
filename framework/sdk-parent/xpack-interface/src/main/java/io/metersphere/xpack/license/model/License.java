package io.metersphere.xpack.license.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class License implements Serializable {

    private String id;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 生成的授权码
     */
    private String licenseCode;
}