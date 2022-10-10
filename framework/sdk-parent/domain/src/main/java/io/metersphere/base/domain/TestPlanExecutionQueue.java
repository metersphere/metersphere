package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanExecutionQueue implements Serializable {
    private String id;

    private String reportId;

    private String runMode;

    private Long createTime;

    private String testPlanId;

    private String resourceId;

    private Integer num;

    private static final long serialVersionUID = 1L;
}