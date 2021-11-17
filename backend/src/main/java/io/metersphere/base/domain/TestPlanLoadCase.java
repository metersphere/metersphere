package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

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

    private String testResourcePoolId;

    private Long order;

    private static final long serialVersionUID = 1L;
}