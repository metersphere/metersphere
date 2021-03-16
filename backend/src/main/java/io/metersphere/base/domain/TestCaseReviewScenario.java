package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReviewScenario implements Serializable {
    private String id;

    private String testCaseReviewId;

    private String apiScenarioId;

    private String status;

    private String environment;

    private Long createTime;

    private Long updateTime;

    private String passRate;

    private String lastResult;

    private String reportId;

    private static final long serialVersionUID = 1L;
}