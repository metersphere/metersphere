package io.metersphere.project.api.assertion.body;

import lombok.Data;

import java.util.List;

@Data
public class ElementCondition {
    private boolean include;
    private boolean typeVerification;
    private boolean arrayVerification;
    private String type;

    List<Condition> conditions;

    public ElementCondition(boolean include, boolean typeVerification, boolean arrayVerification, List<Condition> conditions) {
        this.include = include;
        this.typeVerification = typeVerification;
        this.arrayVerification = arrayVerification;
        this.conditions = conditions;
    }
}
