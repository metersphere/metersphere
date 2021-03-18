package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseReviewScenario implements Serializable {
    private String id;

    private String testCaseReviewId;

    private String apiScenarioId;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String passRate;

    private String lastResult;

    private String reportId;

    private String environment;

    private static final long serialVersionUID = 1L;
}