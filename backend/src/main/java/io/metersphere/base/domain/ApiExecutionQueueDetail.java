package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiExecutionQueueDetail implements Serializable {
    private String id;

    private String queueId;

    private Integer sort;

    private String reportId;

    private String testId;

    private String type;

    private Long createTime;

    private String evnMap;

    private static final long serialVersionUID = 1L;
}