package io.metersphere.sdk.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupResource implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Boolean license = false;

    /**
     * 系统设置、工作空间、项目类型 公用的权限模块
     * e.g. 个人信息
     */
    private boolean global = false;
}
