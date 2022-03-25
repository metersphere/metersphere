package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiExecutionQueue implements Serializable {
    private String id;

    private String reportId;

    private String reportType;

    private String runMode;

    private String poolId;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}