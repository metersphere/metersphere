package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanLoadCase implements Serializable {
    private String id;

    private String testPlanId;

    private String loadCaseId;

    private String loadReportId;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private static final long serialVersionUID = 1L;
}