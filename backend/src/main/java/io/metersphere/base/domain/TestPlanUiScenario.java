package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanUiScenario implements Serializable {
    private String id;

    private String testPlanId;

    private String uiScenarioId;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String passRate;

    private String lastResult;

    private String reportId;

    private String createUser;

    private Long order;

    private String environmentType;

    private String environmentGroupId;

    private String environment;

    private static final long serialVersionUID = 1L;
}