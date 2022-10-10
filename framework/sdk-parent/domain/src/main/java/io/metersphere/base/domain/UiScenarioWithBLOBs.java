package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UiScenarioWithBLOBs extends UiScenario implements Serializable {
    private String scenarioDefinition;

    private String description;

    private String useUrl;

    private String environmentJson;

    private static final long serialVersionUID = 1L;
}