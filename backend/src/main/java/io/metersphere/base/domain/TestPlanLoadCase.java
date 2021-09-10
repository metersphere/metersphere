package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanLoadCase implements Serializable {
    private String id;

    private String testPlanId;

    private String loadCaseId;

    private String status;

    private String loadReportId;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private String testResourcePoolId;

    private Long order;

    private String loadConfiguration;

    private static final long serialVersionUID = 1L;
}