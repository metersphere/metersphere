package io.metersphere.api.dto.scenario.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExtractCommon extends ExtractType {
    private String variable;
    private String value; // value: ${variable}
    private String expression;
    private String description;
    private Boolean multipleMatching;
}
