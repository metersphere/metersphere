package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiScenarioReport implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String scenarioId;

    private String level;

    private String status;

    private String principal;

    private String followPeople;

    private String description;

    private String scenarioDefinition;

    private String userId;

    private Long createTime;

    private Long updateTime;
}