package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReportStructure implements Serializable {
    private String id;

    private String reportId;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}