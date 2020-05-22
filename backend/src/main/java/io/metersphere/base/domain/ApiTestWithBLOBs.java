package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiTestWithBLOBs extends ApiTest implements Serializable {
    private String scenarioDefinition;

    private String schedule;

    private static final long serialVersionUID = 1L;
}