package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiExecutionQueue implements Serializable {
    private String id;

    private String reportId;

    private String reportType;

    private String runMode;

    private String poolId;

    private Long createTime;

    private Boolean failure;

    private static final long serialVersionUID = 1L;
}