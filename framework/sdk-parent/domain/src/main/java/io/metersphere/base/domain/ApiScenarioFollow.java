package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiScenarioFollow implements Serializable {
    private String scenarioId;

    private String followId;

    private static final long serialVersionUID = 1L;
}