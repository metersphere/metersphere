package io.metersphere.project.api.assertion.body;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ElementCondition {
    private boolean include;
    private boolean typeVerification;
    private boolean arrayVerification;
    private String type;

    List<Condition> conditions;
}
