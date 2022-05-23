package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiExecutionQueueDetail implements Serializable {
    private String id;

    private String queueId;

    private Integer sort;

    private String reportId;

    private String testId;

    private String type;

    private Long createTime;

    private Boolean retryEnable;

    private Long retryNumber;

    private String evnMap;

    private static final long serialVersionUID = 1L;
}