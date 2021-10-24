package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioFollow implements Serializable {
    private String scenarioId;

    private String followId;

    private static final long serialVersionUID = 1L;
}